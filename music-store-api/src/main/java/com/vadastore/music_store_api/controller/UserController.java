package com.vadastore.music_store_api.controller;


import com.vadastore.music_store_api.record.*;
import com.vadastore.music_store_api.service.AuthService;
import com.vadastore.music_store_api.service.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {


    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }


    @GetMapping("/me")
    public ResponseEntity<UserDTO> userDetails(@AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();

        UserDTO userDTO = userService.findProfileByEmail(email);

        return ResponseEntity.ok(userDTO);
    }


    @PostMapping("/user/admin/createAdmin")
        public ResponseEntity<SignUpResponse> createAdmin (@Valid @RequestBody SignUpRequest request){
            return new ResponseEntity<>(userService.saveAdmin(request), HttpStatus.CREATED);

        }

}
