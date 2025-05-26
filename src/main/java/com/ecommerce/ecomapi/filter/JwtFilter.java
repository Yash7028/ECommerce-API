package com.ecommerce.ecomapi.filter;

import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.service.UserService;
import com.ecommerce.ecomapi.utility.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars") ||
                "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        System.out.println("Filtering request to: " + request.getRequestURI());

        try {
           String authorizationHeader = request.getHeader("Authorization");
           String username = null;
           String jwt = null;
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
//
//        // Original JWT validation logic
//        final String authHeader = request.getHeader("Authorization");

           if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
               jwt = authorizationHeader.substring(7);
               username = jwtUtil.extractUsername(jwt);
           }

           if (username != null) {
               UserDetails userDetails = userDetailsService.loadUserByUsername(username);
               if (jwtUtil.validateToken(jwt)) {
                   UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                           userDetails.getAuthorities());
                   auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                   SecurityContextHolder.getContext().setAuthentication(auth);

                  User user =  userService.getUserByEmail(username);
                  if (user != null){
                      user.setLastLoginTime(LocalDateTime.now());
                      userService.saveUserBasicsData(user);
                  }
               }
           }
//     response.addHeader("admin", "admin");
           chain.doFilter(request, response);
       } catch (ExpiredJwtException ex) {
           sendError(response, "Token expired", HttpStatus.UNAUTHORIZED);
           return;
       }catch (JwtException | IllegalArgumentException ex) {
           sendError(response, "Invalid token", HttpStatus.UNAUTHORIZED);
           return;
       }
    }

    private void sendError(HttpServletResponse response, String message, HttpStatus status)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(
                String.format("{\"error\": \"%s\", \"message\": \"%s\"}",
                        status.getReasonPhrase(), message)
        );
    }


}
