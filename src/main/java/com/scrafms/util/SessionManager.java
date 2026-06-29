package com.scrafms.util;

import com.scrafms.model.Student;

public class SessionManager {

    private static SessionManager instance;
    private Student currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Student getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Student user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }
}
