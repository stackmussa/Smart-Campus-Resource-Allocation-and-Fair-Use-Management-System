package com.scrafms.controller;

import com.scrafms.model.Student;
import com.scrafms.service.AuthService;

public class AuthController {

    private final AuthService authService = new AuthService();

    public Student login(String username, String password) {
        return authService.login(username, password);
    }

    public void logout() {
        authService.logout();
    }
}
