package com.project.onlineleavemanagementsystem.Repositories;

import com.project.onlineleavemanagementsystem.Entities.LeaveRequest;
import com.project.onlineleavemanagementsystem.Entities.LeaveStatus;
import com.project.onlineleavemanagementsystem.Entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long> {
    List<LeaveRequest> findByStatus(LeaveStatus status);
    Optional<LeaveRequest> findByIdAndStatus(Long id, LeaveStatus status);
    Optional<LeaveRequest> findByIdAndStatusAndUserRole(Long id, LeaveStatus status, Role role);
    long countByManagerIdAndStatus(Long managerId, LeaveStatus status);
}
