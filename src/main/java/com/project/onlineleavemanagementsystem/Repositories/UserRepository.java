package com.project.onlineleavemanagementsystem.Repositories;

import com.project.onlineleavemanagementsystem.Entities.Role;
import com.project.onlineleavemanagementsystem.Entities.User;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(@Email(message = "Invalid email format") String email);
    Optional<User> findByIdAndRole(Long id, Role role);
    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) AND u.role = :role")
    List<User> findByEmailLikeAndRole(@Param("email") String email, @Param("role") Role role);

}
