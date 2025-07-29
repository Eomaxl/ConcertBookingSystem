package com.concertbooking.model;

import java.util.Objects;

public class User {
    private final String id;
    private final String userName;
    private final String email;

    public User(String id, String userName, String email) {
        this.id = id;
        this.userName = userName;
        this.email = email;
    }

    public String getId(){
        return id;
    }

    public String getUserName(){
        return userName;
    }

    public String getEmail(){
        return email;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User user = (User) obj;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
