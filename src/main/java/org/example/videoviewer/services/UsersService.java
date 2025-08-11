package org.example.videoviewer.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.exceptions.AuthenticationException;
import org.example.videoviewer.repositories.UsersRepository;
import org.example.videoviewer.repositories.model.Users;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository repository;

    public List<Users> getUsers() {
        return repository.findAll();
    }

    public Optional<Users> getByUsername(final String username) {
        return repository.findByUsername(username);
    }

    public Users save(final Users user) {
        return repository.save(user);
    }
}
