package com.thefirsthuang.authapp;


public class User {

    public String fullName, age, email,photoUrl;


    public User() {
    //empty constructor
    }

    public User (String fullName, String age, String email, String photoUrl) {
        this.fullName = fullName;
        this.age = age;
        this.email = email;
        this.photoUrl = photoUrl;
    }
    public String getFullName() {
        return fullName;
    }

    public void setFullName (String name) {
        fullName = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl (String url) {
        photoUrl = url;
    }
}
