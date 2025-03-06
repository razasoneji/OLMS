package com.project.onlineleavemanagementsystem.Services;

import com.project.onlineleavemanagementsystem.Entities.User;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;


    public String authenticateUser(String email, String password) {
        log.info("Inside authenticate method of AuthService");
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        log.info("authenticateAdmin: authenticated user " + email);
        User user = (User) authentication.getPrincipal();
        log.info("authenticateAdmin: authenticated user " + user.getUsername());
        return jwtService.generateToken(user.getUsername());
    }
}
