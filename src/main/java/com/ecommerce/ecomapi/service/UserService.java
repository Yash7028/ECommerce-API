package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    public  boolean saveUser(User user);
    public boolean saveUserBasicsData(User user);
    public boolean updateUser(User user, MultipartFile file) throws IOException;
    public User getUserById(Long id);
    public List<User> getAllUser();

    public User getUserByEmail(String email);

    public boolean deleteUserById(Long id);





}
