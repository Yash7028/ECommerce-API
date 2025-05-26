package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.config.CustomUserDetails;
import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.enums.AuthProvider;
import com.ecommerce.ecomapi.enums.Roles;
import com.ecommerce.ecomapi.repository.UserRepo;
import com.ecommerce.ecomapi.serviceImpl.UserServiceImpl;
import com.ecommerce.ecomapi.utility.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/auth/google")
@Tag(
        name = "02. Google Auth APIs",
        description = "Google login or registration page"
)
@Slf4j
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "1. Google auth call ", description = "Get google auth string as request and return access, refresh token. ")
    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        try {

            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", "https://developers.google.com/oauthplayground");
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            System.out.println("working=====================1 ");
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            System.out.println("url of == request " + request);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);
            log.info("Google Token Response: {}", tokenResponse);

            System.out.println("working=====================2 " + tokenResponse);
            String idToken = (String) tokenResponse.getBody().get("id_token");
            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
            System.out.println("working=====================3 ");

            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");
                String name = (String) userInfo.get("name");
       
                Optional<User> optionalUser = userRepository.findByEmail(email);
                User user;
                if (optionalUser.isPresent()){
                    user = optionalUser.get();
                }else{
                    user = new User();
                    user.setName(name != null ? name : email);
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(email));
                    user.setRoles(List.of(Roles.USER));
                    user.setAuthProvider(AuthProvider.GOOGLE);
                    user.setCreatedAt(LocalDateTime.now());
                    userService.saveUserBasicsData(user);
                }

                System.out.println("This is wokring===4");
               CustomUserDetails userDetails = new CustomUserDetails(user.getEmail());
                String accessToken = jwtUtil.accessToken(email);
                String refreshToken = jwtUtil.refreshToken(email);

                user.setRefreshToken(refreshToken);
                user.setLastLoginTime(LocalDateTime.now());
                userService.saveUserBasicsData(user);

                System.out.println("This is wokring===5");

                Map<String,Object> response = Map.of(
                        "accessToken", accessToken,
                        "refreshToken" , refreshToken
                );

                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
