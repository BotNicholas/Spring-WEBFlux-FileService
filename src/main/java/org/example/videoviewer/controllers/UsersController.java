package org.example.videoviewer.controllers;

import lombok.RequiredArgsConstructor;
import org.example.videoviewer.repositories.model.Users;
import org.example.videoviewer.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;

    @GetMapping
    public ResponseEntity<List<Users>> getUsers() {
        return ResponseEntity.ok(usersService.getUsers());
    }
}
