package com.example.backend.repositories;

import com.example.backend.models.Country;
import com.example.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);

    Optional<User> findByToken(String s);
}
