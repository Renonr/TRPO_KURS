package com.example.airlines.Controllers;

import com.example.airlines.DTO.TicketDTO;
import com.example.airlines.DTO.UserDTO;
import com.example.airlines.Models.User;
import com.example.airlines.Repositories.UserRepository;
import com.example.airlines.Services.FileStorageService;
import com.example.airlines.Services.TicketService;
import com.example.airlines.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final TicketService ticketService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<UserDTO> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        UserDTO userDTO = userService.convertToDTO(user);
        if (user.getProfileImageUrl() != null) {
            // Добавляем базовый путь только при отдаче DTO
            userDTO.setProfileImageUrl("/api/profile/images/" + user.getProfileImageUrl());
        }
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<UserDTO> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            User user = (User) authentication.getPrincipal();
            String fileName = fileStorageService.storeFile(file);
            // Сохраняем только имя файла, без пути
            user.setProfileImageUrl(fileName);
            User savedUser = userRepository.save(user);

            UserDTO userDTO = userService.convertToDTO(savedUser);
            // Добавляем полный URL только в DTO
            userDTO.setProfileImageUrl("/api/profile/images/" + fileName);

            return ResponseEntity.ok(userDTO);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String fileName) {

        Resource resource = fileStorageService.loadFile(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .body(resource);
    }
}
