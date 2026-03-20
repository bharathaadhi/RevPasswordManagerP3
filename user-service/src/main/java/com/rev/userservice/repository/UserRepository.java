package com.rev.userservice.repository;
import java.util.Optional;
import com.rev.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findFirstByEmail(@Param("email") String email);
 
    @Query("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:name)")
    Optional<User> findFirstByName(@Param("name") String name);
 
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmail(@Param("email") String email);
}