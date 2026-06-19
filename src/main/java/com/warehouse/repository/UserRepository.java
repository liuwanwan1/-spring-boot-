package com.warehouse.repository;

import com.warehouse.entity.User;
import com.warehouse.entity.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUsernameAndStatus(String username, UserStatus status);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
