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


    @Query("SELECT lr.leaveType, COUNT(lr) FROM LeaveRequest lr " +
            "WHERE lr.status = 'APPROVED' AND lr.user.role = 'MANAGER' " +
            "GROUP BY lr.leaveType")
    List<Object[]> countApprovedLeavesByTypeForManagers();

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr " +
            "WHERE lr.status = 'REJECTED' AND lr.user.role = 'MANAGER'")
    Long countRejectedLeavesForManagers();

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr " +
            "WHERE lr.status = 'PENDING' AND lr.user.role = 'MANAGER'")
    Long countPendingLeavesForManagers();


    List<User> findByManager(User manager);
    Optional<User> findByEmailAndManager(String email, User manager);


    // Search employees whose email contains the given input and they should be under the given manager
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) AND u.manager = :manager")
    List<User> findByEmailLikeAndManager(@Param("email") String email, @Param("manager") User manager);

    long countByRole(Role role);

    long countByManagerId(Long managerId);
    boolean existsByEmail(String email);
}
