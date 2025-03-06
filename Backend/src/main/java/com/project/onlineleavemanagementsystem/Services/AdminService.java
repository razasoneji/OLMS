//package com.project.onlineleavemanagementsystem.Services;
//
//public class AdminService {
//}
package com.project.onlineleavemanagementsystem.Services;

import com.project.onlineleavemanagementsystem.Entities.*;
import com.project.onlineleavemanagementsystem.Repositories.HolidayRepository;
import com.project.onlineleavemanagementsystem.Repositories.LeaveBalanceRepository;
import com.project.onlineleavemanagementsystem.Repositories.LeaveRequestRepository;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private HolidayRepository holidayRepository;


    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;



    public User getAdminById(Long id) {
        return userRepository.findByIdAndRole(id, Role.ADMIN)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found with ID: " + id));
    }


    public List<User> getAllManagers() {
        return userRepository.findByRole(Role.MANAGER);
    }

    public User getManagerById(Long id) {
        return userRepository.findByIdAndRole(id, Role.MANAGER)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found with ID: " + id));
    }


//    public User createManager(User manager) {
//        // Fetch the admin (assuming there is only one admin with ID 1)
//        User admin = userRepository.findByIdAndRole(1L, Role.ADMIN)
//                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));
//
//        // Set the role as MANAGER and assign admin as manager's manager
//        manager.setRole(Role.MANAGER);
//        manager.setManager(admin);
//        LeaveBalance leaveBalance = new LeaveBalance();
//        leaveBalance.setUser(admin);
//        manager.setLeaveBalance(new LeaveBalance());
//
//        // Hash the password before saving
//        manager.setPassword(passwordEncoder.encode(manager.getPassword()));
//
//        return userRepository.save(manager);
//    }

    public User createManager(User manager) {
        // Fetch the admin (assuming there is only one admin with ID 1)
        User admin = userRepository.findByIdAndRole(1L, Role.ADMIN)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        // Set the role as MANAGER and assign admin as manager's manager
        manager.setRole(Role.MANAGER);
        manager.setManager(admin);

        // Create and assign leave balance to the new manager
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setUser(manager); // Set the newly created manager as the user
        manager.setLeaveBalance(leaveBalance);

        // Hash the password before saving
        manager.setPassword(passwordEncoder.encode(manager.getPassword()));

        // Save manager first (cascading may handle leave balance if mapped)
        return userRepository.save(manager);
    }


    public void deleteManager(Long id) {
        User manager = userRepository.findByIdAndRole(id, Role.MANAGER)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found with ID: " + id));
        userRepository.delete(manager);
    }


    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatus(status);
    }


//    public boolean updateLeaveRequestStatus(Long leaveRequestId, LeaveStatus status) {
//        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
//                .orElseThrow(() -> new RuntimeException("Leave request not found"));
//
//        if (!leaveRequest.getStatus().equals(LeaveStatus.PENDING)) {
//            return false; // Can only approve/reject pending requests
//        }
//
//        leaveRequest.setStatus(status);
//        leaveRequestRepository.save(leaveRequest);
//        return true;
//    }



    // up1
    @Transactional
    public boolean updateManagerLeaveStatus(Long leaveRequestId, LeaveStatus status) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndStatusAndUserRole(leaveRequestId, LeaveStatus.PENDING, Role.MANAGER).orElse(null);

        if (leaveRequest == null) {
            return false;
        }

        if (status == LeaveStatus.REJECTED) {
            leaveRequest.setStatus(LeaveStatus.REJECTED);
            leaveRequestRepository.save(leaveRequest);
            return true;
        }

        int totalDays = (int) ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        int holidays = holidayRepository.countHolidaysBetween(leaveRequest.getStartDate(), leaveRequest.getEndDate());
        int actualLeaveDays = totalDays - holidays;

        LeaveBalance leaveBalance = leaveBalanceRepository.findByUser(leaveRequest.getUser())
                .orElse(null);

        if (leaveBalance == null || leaveBalance.getRemainingLeaves() < actualLeaveDays) {
            return false; // Insufficient leave balance
        }

        leaveBalance.setRemainingLeaves(leaveBalance.getRemainingLeaves() - actualLeaveDays);

        // Deduct from the specific leave type field
        if (leaveRequest.getLeaveType() == LeaveType.SICK) {
            leaveBalance.setSickLeaves(leaveBalance.getSickLeaves() - actualLeaveDays);
        } else if (leaveRequest.getLeaveType() == LeaveType.CASUAL) {
            leaveBalance.setCasualLeaves(leaveBalance.getCasualLeaves() - actualLeaveDays);
        } else if (leaveRequest.getLeaveType() == LeaveType.UNPAID) {
            leaveBalance.setUnpaidLeaves(leaveBalance.getUnpaidLeaves() - actualLeaveDays);
        } else if (leaveRequest.getLeaveType() == LeaveType.PAID) {
            leaveBalance.setPaidLeaves(leaveBalance.getPaidLeaves() - actualLeaveDays);
        }

        leaveBalanceRepository.save(leaveBalance);
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequestRepository.save(leaveRequest);
        return true;
    }

    // up 2
    public LeaveBalance getEmployeeLeaveBalance(Long employeeId) {
        return leaveBalanceRepository.findByUserId(employeeId).orElse(null);
    }



    public List<User> searchManagersByEmail(String email) {
        return userRepository.findByEmailLikeAndRole(email, Role.MANAGER);
    }

    public Map<String, Integer> getTotalApprovedLeavesForManagers() {
        List<Object[]> results = userRepository.countApprovedLeavesByTypeForManagers();

        Map<String, Integer> leaveCounts = new HashMap<>();
        leaveCounts.put("TOTAL_APPROVED_LEAVES", 0);
        leaveCounts.put("PAID_LEAVE", 0);
        leaveCounts.put("SICK_LEAVE", 0);
        leaveCounts.put("CASUAL_LEAVE", 0);
        leaveCounts.put("UNPAID_LEAVE", 0);

        for (Object[] row : results) {
            LeaveType leaveType = (LeaveType) row[0];
            Integer count = ((Number) row[1]).intValue();

            leaveCounts.put(leaveType.name(), count);
            leaveCounts.put("TOTAL_APPROVED_LEAVES", leaveCounts.get("TOTAL_APPROVED_LEAVES") + count);
        }

        return leaveCounts;
    }




    public Long getRejectedLeavesForManagers() {
        return userRepository.countRejectedLeavesForManagers();
    }

    public Long getPendingLeavesForManagers() {
        return userRepository.countPendingLeavesForManagers();
    }


    @Transactional
    public boolean increaseManagerCredits(Long managerId, int creditUnits) {
        if (creditUnits < 1 || creditUnits > 5) {
            throw new IllegalArgumentException("Credits must be between 1 and 5.");
        }

        // Find the manager
        User manager = userRepository.findByIdAndRole(managerId, Role.MANAGER)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

        // Get their leave balance
        LeaveBalance leaveBalance = manager.getLeaveBalance();
        if (leaveBalance == null) {
            LeaveBalance newLeaveBalance = new LeaveBalance();
            newLeaveBalance.setUser(manager);
            manager.setLeaveBalance(newLeaveBalance);
        }

        // Increase credits, remaining leaves, and paid leaves
        leaveBalance.setCredits(leaveBalance.getCredits() + creditUnits);
        leaveBalance.setRemainingLeaves(leaveBalance.getRemainingLeaves() + creditUnits);
        leaveBalance.setPaidLeaves(leaveBalance.getPaidLeaves() + creditUnits);

        userRepository.save(manager);
        return true;
    }

    public long getTotalManagersCount() {
        return userRepository.countByRole(Role.MANAGER);
    }
}
