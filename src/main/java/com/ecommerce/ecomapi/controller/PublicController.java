package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.LoginRequest;
import com.ecommerce.ecomapi.dto.RequestTokenDto;
import com.ecommerce.ecomapi.dto.UserDto;
import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.enums.Gender;
import com.ecommerce.ecomapi.service.UserService;
import com.ecommerce.ecomapi.utility.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/public")
@Validated
@Tag(
        name = "01. Public APIs",
        description = "Endpoints for health check, user registration, login, and token refresh operations."
)
public class PublicController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ModelMapper modelMapper;

    @Operation(summary = "1. Health check ", description = "Check the overall health of APIs.")
    @GetMapping("/healthCheck")
    public ResponseEntity<String> healthCheck(){
        return new ResponseEntity<>("Health is ok of Ecommerce API", HttpStatus.OK);
    }

    /* Create user */
    @Operation(summary = "2. Create User", description = "Create user and return ok as response.")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        user.setGender(Gender.fromStringIgnoreCase(userDto.getGender()));

        if (userService.saveUser(user)){
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /* Login user */
    @Operation(summary = "3. Login user", description = "Authenticate user and return JWT token.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println(loginRequest.getEmail() + loginRequest.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userService.getUserByEmail(loginRequest.getEmail());
            String accessToken = jwtUtil.accessToken(loginRequest.getEmail());
            String refreshToken = jwtUtil.refreshToken(loginRequest.getEmail());

            user.setRefreshToken(refreshToken);
            userService.saveUserBasicsData(user);

            Map<String,Object> response = Map.of(
                    "accessToken", accessToken,
                    "refreshToken" , refreshToken
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    /* Refresh token */
    @Operation(summary = "4. Refresh token",
            description = "Send refresh token as request and get response with new access and refresh tokens.")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RequestTokenDto request) {
        try {
           String refreshToken = request.getRefreshToken();

           if (!jwtUtil.validateToken(refreshToken)) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
           }

           String email = jwtUtil.extractUsername(refreshToken); // fixed method name
           User user = userService.getUserByEmail(email);

           if (!refreshToken.equals(user.getRefreshToken())) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token mismatch");
           }

           String newAccessToken = jwtUtil.accessToken(email);
           return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
       } catch (ExpiredJwtException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired refresh token, please login again");
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong: " + e.getMessage());
       }
    }


}

