package com.project.onlineleavemanagementsystem.Controllers;

import com.project.onlineleavemanagementsystem.Entities.User;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import com.project.onlineleavemanagementsystem.Services.AdminService;
import com.project.onlineleavemanagementsystem.Services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;


//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
//        log.info("Inside the auth controller , login , login email: " + email + " password: " + password);
//        String token = authService.authenticateUser(email, password);
//        return ResponseEntity.ok(token);
//    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String email, @RequestParam String password) {
        log.info("Inside the auth controller, login. Email: " + email);

        // Authenticate and get the JWT token
        String token = authService.authenticateUser(email, password);

        // Fetch user details
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prepare the response map
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole().name()); // Assuming Role is an ENUM (ADMIN, MANAGER, EMPLOYEE)

        return ResponseEntity.ok(response);
    }



    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok(null); // Returning null as a string
    }
}
