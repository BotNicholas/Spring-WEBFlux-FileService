package org.example.videoviewer.repositories;

import org.example.videoviewer.repositories.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    public Optional<Users> findByUsername(String username);
}
