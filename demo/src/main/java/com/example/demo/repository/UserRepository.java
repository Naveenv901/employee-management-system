package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Page<UserEntity> findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name, String city, Pageable pageable);

    public boolean existsByEmail(String email);
}