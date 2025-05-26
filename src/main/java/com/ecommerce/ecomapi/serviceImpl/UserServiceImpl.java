package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.enums.AuthProvider;
import com.ecommerce.ecomapi.enums.Gender;
import com.ecommerce.ecomapi.enums.Roles;
import com.ecommerce.ecomapi.exception.EmailAlreadyExistsException;
import com.ecommerce.ecomapi.exception.UserNotFoundException;
import com.ecommerce.ecomapi.repository.ProductRepo;
import com.ecommerce.ecomapi.repository.UserRepo;
import com.ecommerce.ecomapi.service.CloudinaryService;
import com.ecommerce.ecomapi.service.ProductService;
import com.ecommerce.ecomapi.service.UserService;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SecurityUtil securityUtil;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProductService productService;

    /* Save User */
    @Override
    public boolean saveUser(User user) {

        try {
            getUserByEmail(user.getEmail());
            throw new EmailAlreadyExistsException("Email already exists!");
        } catch (UserNotFoundException ex) {
            // this is okay — email doesn't exist, so proceed to save
            user.setRoles(user.getRoles() != null ? user.getRoles() : List.of(Roles.USER));

            if (user.getGender() == Gender.MALE) {
                user.setProfilePic("https://res.cloudinary.com/dauaakon4/image/upload/v1747311712/o0ag2cjhel5pxb6znyxn.jpg");
            } else if (user.getGender() == Gender.FEMALE) {
                user.setProfilePic("https://res.cloudinary.com/dauaakon4/image/upload/v1747312093/juiukjquurhjbf0uckf7.jpg");
            } else {
                user.setProfilePic("https://res.cloudinary.com/dauaakon4/image/upload/v1747312628/oomejiu2rv0dm2pruae8.jpg");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAuthProvider(AuthProvider.SELF);
            user.setCreatedAt(LocalDateTime.now());
            userRepo.save(user);

            return true;
        }
    }

    /* Save user basic info */
    public boolean saveUserBasicsData(User user) {
    try {
        userRepo.save(user);
        return true;
    }catch (Exception e){
        return false;
    }
    }

    /* Update user */
    @Override
    public boolean updateUser(User updatedData, MultipartFile file) throws IOException {
        Long id = securityUtil.getCurrentUserId();  // Centralized user ID
        User user = getUserById(id);

        if (user == null) return false;
        // Handle profile pic upload
        if (file != null && !file.isEmpty()) {
            // Delete previous image if exists
            if (user.getProfilePic() != null) {
                boolean isDeleted = cloudinaryService.deleteImageThroughPublicId(user.getProfilePic());
                if (!isDeleted) return false;
            }
            // Upload new image
            String newImageUrl = cloudinaryService.uploadImage(file);
            user.setProfilePic(newImageUrl);
        }
        // Update fields only if they are not null
        if (updatedData.getName() != null) user.setName(updatedData.getName());
        if (updatedData.getPassword() != null) user.setPassword(updatedData.getPassword());
        if (updatedData.getPhoneNumber() != null) user.setPhoneNumber(updatedData.getPhoneNumber());
        if (updatedData.getGender() != null) user.setGender(updatedData.getGender());


        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);

        return true;
    }

    /* Get user by id*/
    @Override
    public User getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new UserNotFoundException("User is not found with this id : " + id));
    }

    /* Get all user */
    @Override
    public List<User> getAllUser() {
        return userRepo.findAll();
    }

    /* Get user by email id */
    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User is not found with this email : " + email));
    }

    /* Delete user by id */
    @Override
    public boolean deleteUserById(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException("User is not found with this id : " + id));

        boolean isSELLER = securityUtil.getCurrentUserAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SELLER"));

        if (isSELLER){
            productService.deleteProductBySellerId(String.valueOf(id));
        }

        if(user != null){
            userRepo.deleteById(id);
            return true;
        }
        return false;
    }
}
