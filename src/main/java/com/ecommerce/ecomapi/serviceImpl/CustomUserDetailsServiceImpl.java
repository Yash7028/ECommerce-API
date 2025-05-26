package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.config.CustomUserDetails;
import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            System.out.println("User found: " + email + ", Encoded Password: " + user.getPassword());

//            return org.springframework.security.core.userdetails.User
//                    .builder()
//                    .username(user.getEmail())
//                    .password(user.getPassword())
//                    .authorities(user.getRoles().stream()
//                            .map(role -> "ROLE_" + role)
//                            .toArray(String[]::new))
//                    .build();

            List<String> roles = user.getRoles()
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());

            return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), roles);

        } catch (Exception e) {
            e.printStackTrace(); // Catch the real reason
            throw e;
        }
    }

}
