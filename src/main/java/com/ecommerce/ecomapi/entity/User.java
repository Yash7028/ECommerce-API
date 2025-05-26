package com.ecommerce.ecomapi.entity;


import com.ecommerce.ecomapi.enums.AuthProvider;
import com.ecommerce.ecomapi.enums.Gender;
import com.ecommerce.ecomapi.enums.Roles;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"addresses", "orders"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 32)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(length = 15)
    private String phoneNumber;

    @Column(length = 1000)
    private String profilePic;

    @Enumerated(value = EnumType.STRING)
    private AuthProvider authProvider;


    @Getter(value = AccessLevel.NONE)
    private boolean accountIsEnabled = true;

    private boolean emailVerified = false;

    private boolean phoneVerified = false;
    /*Newly added*/
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @Column(length = 1000)
    @JsonIgnore
    private String refreshToken;

    private boolean accountNonLocked = true;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    /*end*/

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<Roles> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonManagedReference
    @JsonIgnore
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Cart> carts = new ArrayList<>();
}
