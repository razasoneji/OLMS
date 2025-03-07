package com.project.onlineleavemanagementsystem.Repositories;

import com.project.onlineleavemanagementsystem.Entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long> {
    List<LeaveRequest> findByStatus(LeaveStatus status);
    Optional<LeaveRequest> findByIdAndStatus(Long id, LeaveStatus status);
    Optional<LeaveRequest> findByIdAndStatusAndUserRole(Long id, LeaveStatus status, Role role);
    long countByManagerIdAndStatus(Long managerId, LeaveStatus status);
    List<LeaveRequest> findByManagerIdAndStatus(Long managerId, LeaveStatus status);
    List<LeaveRequest> findByUserIdAndStatus(Long userId, LeaveStatus status);
    List<LeaveRequest> findByUserAndLeaveTypeAndStatus(User user, LeaveType leaveType, LeaveStatus status);
    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l WHERE l.user = :user AND l.leaveType = :leaveType " +
            "AND l.status IN :statuses AND l.startDate <= :endDate AND l.endDate >= :startDate")
    boolean existsByUserAndLeaveTypeAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            @Param("user") User user,
            @Param("leaveType") LeaveType leaveType,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("endDate") LocalDate endDate,
            @Param("startDate") LocalDate startDate);


    long countByUser(User user);

    long countByUserAndStatus(User user, LeaveStatus status);

    List<LeaveRequest> findByUserAndStatus(User user, LeaveStatus status);

    Optional<LeaveRequest> findByIdAndUser(Long id, User user);

    void deleteById(Long id);





}
