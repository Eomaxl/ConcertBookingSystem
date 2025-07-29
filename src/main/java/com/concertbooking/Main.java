package com.concertbooking;

import com.concertbooking.exception.SeatNotAvailableException;
import com.concertbooking.model.*;
import com.concertbooking.repository.BookingRepository;
import com.concertbooking.repository.ConcertRepository;
import com.concertbooking.repository.impl.BookingRepositoryImpl;
import com.concertbooking.repository.impl.ConcertRepositoryImpl;
import com.concertbooking.system.ConcertTicketBookingSystem;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ConcertTicketBookingSystem system = ConcertTicketBookingSystem.getInstance();

    // Separate service instances for testing different concurrency mechanisms
    private static final BookingService originalBookingService = new BookingService(new BookingRepositoryImpl(),
            new ConcertRepositoryImpl());
    private static final BookingServiceWithLock bookingServiceWithReentrantLock = new BookingServiceWithLock(
            new BookingRepository(), new ConcertRepository());

    // Global storage for users to be accessible by both services/tests
    private static final Map<String, User> users = new HashMap<>();

    public static void main(String[] args) {
        seedData();
        runCli();
    }

    private static void seedData() {
        // Create users and store them globally
        User user1 = new User(UUID.randomUUID().toString(), "Alice", "alice@example.com");
        User user2 = new User(UUID.randomUUID().toString(), "Bob", "bob@example.com");
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);

        // Create seats
        List<Seat> concert1Seats = List.of(
                new Seat(UUID.randomUUID().toString(), "A1", SeatType.REGULAR, 50.0),
                new Seat(UUID.randomUUID().toString(), "A2", SeatType.REGULAR, 50.0),
                new Seat(UUID.randomUUID().toString(), "B1", SeatType.PREMIUM, 80.0),
                new Seat(UUID.randomUUID().toString(), "C1", SeatType.VIP, 120.0));
        List<Seat> concert2Seats = List.of(
                new Seat(UUID.randomUUID().toString(), "D1", SeatType.REGULAR, 40.0),
                new Seat(UUID.randomUUID().toString(), "D2", SeatType.REGULAR, 40.0));

        // Create concerts
        Concert concert1 = new Concert(UUID.randomUUID().toString(), "Band X", "Venue A",
                LocalDateTime.now().plusDays(7), concert1Seats);
        Concert concert2 = new Concert(UUID.randomUUID().toString(), "Singer Y", "Venue B",
                LocalDateTime.now().plusDays(14), concert2Seats);

        // Add concerts to the main system's repository. Note: in a real app, repos are
        // passed to services.
        // For this demo, we'll directly add them to the system's underlying repository.
        system.addConcert(concert1);
        system.addConcert(concert2);

        // Also manually add concerts to the separate services' repositories for testing
        originalBookingService.getConcertRepository().addConcert(concert1);
        originalBookingService.getConcertRepository().addConcert(concert2);
        bookingServiceWithReentrantLock.getConcertRepository().addConcert(concert1);
        bookingServiceWithReentrantLock.getConcertRepository().addConcert(concert2);

        System.out.println("--- Seed Data Loaded ---");
        System.out.println("Concert 1: " + concert1.getArtistName() + " at " + concert1.getVenue() + " (ID: "
                + concert1.getId() + ")");
        System.out.println("Concert 2: " + concert2.getArtistName() + " at " + concert2.getVenue() + " (ID: "
                + concert2.getId() + ")");
        System.out.println("User 1: " + user1.getUserName() + " (ID: " + user1.getId() + ")");
        System.out.println("User 2: " + user2.getUserName() + " (ID: " + user2.getId() + ")");
    }

    private static void runCli() {
        while (true) {
            System.out.println("\n--- Concert Ticket Booking System CLI ---");
            System.out.println("1. Search Concerts by Artist");
            System.out.println("2. Search Concerts by Venue");
            System.out.println("3. Book Tickets (using AtomicReference)");
            System.out.println("4. Cancel Booking (using AtomicReference)");
            System.out.println("5. Run Concurrency Test (AtomicReference)");
            System.out.println("6. Run Concurrency Test (ReentrantLock)");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    searchConcertsByArtist();
                    break;
                case 2:
                    searchConcertsByVenue();
                    break;
                case 3:
                    bookTickets(); // Uses system.bookTickets which uses originalBookingService
                    break;
                case 4:
                    cancelBooking(); // Uses system.cancelBooking which uses originalBookingService
                    break;
                case 5:
                    runConcurrencyTest(originalBookingService, "AtomicReference");
                    break;
                case 6:
                    runConcurrencyTest(bookingServiceWithReentrantLock, "ReentrantLock");
                    break;
                case 7:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void searchConcertsByArtist() {
        System.out.print("Enter artist name: ");
        String artist = scanner.nextLine();
        List<Concert> concerts = system.searchConcertsByArtist(artist);
        if (concerts.isEmpty()) {
            System.out.println("No concerts found for artist: " + artist);
        } else {
            System.out.println("Concerts by " + artist + ":");
            concerts.forEach(c -> System.out
                    .println("  ID: " + c.getId() + ", Venue: " + c.getVenue() + ", Date: " + c.getDateTime()));
        }
    }

    private static void searchConcertsByVenue() {
        System.out.print("Enter venue name: ");
        String venue = scanner.nextLine();
        List<Concert> concerts = system.searchConcertsByVenue(venue);
        if (concerts.isEmpty()) {
            System.out.println("No concerts found for venue: " + venue);
        } else {
            System.out.println("Concerts at " + venue + ":");
            concerts.forEach(c -> System.out
                    .println("  ID: " + c.getId() + ", Artist: " + c.getArtistName() + ", Date: " + c.getDateTime()));
        }
    }

    private static void bookTickets() {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        User currentUser = users.get(userId); // Get user from global map
        if (currentUser == null) {
            System.err.println("User not found with ID: " + userId);
            return;
        }
        System.out.print("Enter Concert ID: ");
        String concertId = scanner.nextLine();
        System.out.print("Enter Seat IDs (comma-separated): ");
        List<String> seatIds = Arrays.asList(scanner.nextLine().split(",\s*"));

        try {
            // Use the main system's booking service
            Booking booking = system.bookTickets(userId, concertId, seatIds, currentUser);
            System.out.println("Booking successful! Booking ID: " + booking.getId() + ", Total Price: "
                    + booking.getTotalPrice() + ", Status: " + booking.getStatus());
        } catch (SeatNotAvailableException | IllegalArgumentException e) {
            System.err.println("Booking failed: " + e.getMessage());
        }
    }

    private static void cancelBooking() {
        System.out.print("Enter Booking ID to cancel: ");
        String bookingId = scanner.nextLine();
        // Use the main system's booking service
        if (system.cancelBooking(bookingId)) {
            System.out.println("Booking " + bookingId + " cancelled successfully.");
        } else {
            System.out.println("Booking " + bookingId + " not found or could not be cancelled.");
        }
    }

    private static void runConcurrencyTest(Object serviceInstance, String testType) {
        System.out.println("\n--- Running Concurrency Test (" + testType + ") ---");
        System.out.println("Attempting to book a single seat (A1 from Band X concert) concurrently from 5 threads.");

        // Retrieve test concert and seat from the appropriate repository instance
        // Resetting data for each test run requires re-seeding the entire application
        // state
        // For simplicity in this CLI, you should restart the app or ensure the seat is
        // available.
       ConcertRepository repoForTest;
        if (serviceInstance instanceof BookingService) {
            repoForTest = (ConcertRepository) ((BookingService) serviceInstance).getConcertRepository();
        } else if (serviceInstance instanceof BookingServiceWithLock) {
            repoForTest = (ConcertRepository) ((BookingServiceWithLock) serviceInstance).getConcertRepository();
        } else {
            System.err.println("Unknown service instance type.");
            return;
        }

        Optional<Concert> concertOpt = repoForTest.searchConcertsByArtist("Band X").stream().findFirst();
        if (concertOpt.isEmpty()) {
            System.out.println("Concurrency test setup failed: 'Band X' concert not found in " + testType
                    + " service's repository.");
            return;
        }
        Concert testConcert = concertOpt.get();

        // Find the specific seat for the test (must be available)
        Optional<Seat> seatATestOpt = testConcert.getSeats().stream()
                .filter(s -> s.getSeatNumber().equals("A1") && s.getStatus() == SeatStatus.AVAILABLE)
                .findFirst();

        if (seatATestOpt.isEmpty()) {
            System.out.println("Concurrency test setup failed: Seat A1 in 'Band X' concert not available for "
                    + testType + " test. Please restart application to reset data.");
            return;
        }
        Seat seatToBookConcurrently = seatATestOpt.get();

        User testUser = users.get(users.keySet().iterator().next()); // Use an existing user for the test
        if (testUser == null) {
            System.err.println("Concurrency test setup failed: No users found.");
            return;
        }

        int numberOfThreads = 5;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " attempting to book seat "
                            + seatToBookConcurrently.getId());
                    if (serviceInstance instanceof BookingService) {
                        ((BookingService) serviceInstance).bookSeats(testUser.getId(), testConcert.getId(),
                                List.of(seatToBookConcurrently.getId()), testUser);
                    } else if (serviceInstance instanceof BookingServiceWithLock) {
                        ((BookingServiceWithLock) serviceInstance).bookSeats(testUser.getId(), testConcert.getId(),
                                List.of(seatToBookConcurrently.getId()), testUser);
                    }
                    successCount.incrementAndGet();
                    System.out.println(Thread.currentThread().getName() + " booked successfully!");
                } catch (SeatNotAvailableException e) {
                    System.out.println(Thread.currentThread().getName() + " failed to book: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println(Thread.currentThread().getName() + " encountered an error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(10, TimeUnit.SECONDS); // Wait for all threads to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Concurrency test interrupted.");
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n--- Concurrency Test Results (" + testType + ") ---");
        System.out.println("Total booking attempts: " + numberOfThreads);
        System.out.println("Successful bookings: " + successCount.get());
        System.out.println("Failed bookings (Seat Not Available): " + (numberOfThreads - successCount.get()));
        System.out.println("------------------------------------------");

        // Note: To re-run the concurrency test on the same seat, you would need to
        // restart the application to re-seed data.
        // The internal state of services might be modified.
        System.out.println(
                "Note: To re-run this concurrency test, please restart the application to re-seed data and reset seat status.");
    }

    // Helper method for BookingService to get its concert repository
    // This is a hack for the demo; in a real app, you'd dependency inject
    // repositories.
    static class BookingService extends com.concertbooking.service.BookingService {
        public BookingService(BookingRepository bookingRepository,
                              ConcertRepository concertRepository) {
            super(bookingRepository, concertRepository);
        }

        public ConcertRepository getConcertRepository() {
            return (ConcertRepository) super.concertRepository;
        }
    }

    // Helper method for BookingServiceWithLock to get its concert repository
    static class BookingServiceWithLock extends com.concertbooking.service.BookingServiceWithLock {
        public BookingServiceWithLock(BookingRepository bookingRepository,
                                      ConcertRepository concertRepository) {
            super(bookingRepository, concertRepository);
        }

        public ConcertRepository getConcertRepository() {
            return (ConcertRepository) super.concertRepository;
        }
    }
}
