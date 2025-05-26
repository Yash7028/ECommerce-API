package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.UserUpdateDto;
import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.enums.Gender;
import com.ecommerce.ecomapi.service.UserService;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Tag(
        name = "03. User APIs",
        description = "Endpoints for managing and accessing user info, including create, update, delete, and fetch operations."
)
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private SecurityUtil securityUtil;

    /* Get user by id */
    @Operation(
            summary = "Get Current User Information",
            description = "Retrieves the profile information of the currently authenticated user, including name, email, profile picture, phone number, and gender."
    )
    @GetMapping("/getUser")
    public ResponseEntity<?> getUser() {
        Long id = securityUtil.getCurrentUserId();
        User user = userService.getUserById(id);

        try {
            Map<String, Object> responseBody = Map.of(
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "profilePic",user.getProfilePic(),
                    "phoneNumber",user.getPhoneNumber(),
                    "gender",user.getGender()
            );

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            e.getStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* update user info like profile picture and basic info. */
    @Operation(
            summary = "Update user profile information",
            description = "Allows the authenticated user to update their profile information, including name, password, phone number, gender, and optionally a profile picture."
    )
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(@RequestParam(value = "file", required = false) MultipartFile file,
                                        @ModelAttribute UserUpdateDto userUpdateDto) throws IOException {
        User user = modelMapper.map(userUpdateDto, User.class);

        if (userUpdateDto.getGender() != null) user.setGender(Gender.fromStringIgnoreCase(userUpdateDto.getGender()));

        if (userService.updateUser(user, file)) {
            return ResponseEntity.ok("Data updated successfully");
        }
        return ResponseEntity.badRequest().body("Update failed");
    }

    /* Delete user by id */
    @Operation(
            summary = "Delete User Account",
            description = "Deletes the currently authenticated user's account along with all associated data such as profile and related records. "
                    + "This action is irreversible and requires the user to be authenticated."
    )
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        Long id = securityUtil.getCurrentUserId();

        if (userService.deleteUserById(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}

//@Operation(
//        summary = "Update user profile information",
//        description = "Allows the authenticated user to update their profile information, including name, password, phone number, gender, and optionally a profile picture.",
//        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                description = "User data and optional profile picture to update",
//                required = true,
//                content = @Content(
//                        mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
//                        schema = @Schema(implementation = UserUpdateDto.class)
//                )
//        ),
//        responses = {
//                @ApiResponse(responseCode = "200", description = "User data updated successfully"),
//                @ApiResponse(responseCode = "400", description = "Update failed due to invalid input"),
//                @ApiResponse(responseCode = "500", description = "Internal server error")
//        }
//)
