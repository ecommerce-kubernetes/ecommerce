package com.example.userservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Long> {

    Optional<UserEntity> findById(Long userId);

    Optional<UserEntity> findByEmail(String email);
}
