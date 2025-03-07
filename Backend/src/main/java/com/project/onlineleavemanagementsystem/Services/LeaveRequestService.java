package com.project.onlineleavemanagementsystem.Services;


import com.project.onlineleavemanagementsystem.Entities.*;
import com.project.onlineleavemanagementsystem.Repositories.HolidayRepository;
import com.project.onlineleavemanagementsystem.Repositories.LeaveBalanceRepository;
import com.project.onlineleavemanagementsystem.Repositories.LeaveRequestRepository;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserService userService;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final HolidayRepository holidayRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public Map<String, Long> getLeaveRequestSummaryByManager(Long managerId) {
        long acceptedCount = leaveRequestRepository.countByManagerIdAndStatus(managerId, LeaveStatus.APPROVED);
        long rejectedCount = leaveRequestRepository.countByManagerIdAndStatus(managerId, LeaveStatus.REJECTED);
        long pendingCount = leaveRequestRepository.countByManagerIdAndStatus(managerId, LeaveStatus.PENDING);

        Map<String, Long> summary = new HashMap<>();
        summary.put("accepted", acceptedCount);
        summary.put("rejected", rejectedCount);
        summary.put("pending", pendingCount);

        return summary;
    }

    public List<LeaveRequest> getLeaveRequestsByManagerAndStatus(Long managerId, LeaveStatus status) {
        return leaveRequestRepository.findByManagerIdAndStatus(managerId, status);
    }

    public List<LeaveRequest> getLeaveRequestsByUserAndStatus(Long managerId, LeaveStatus status) {
        return leaveRequestRepository.findByUserIdAndStatus(managerId, status);
    }

//    @Transactional
//    public void applyLeave(String managerEmail, LocalDate startDate, LocalDate endDate, LeaveType leaveType) {
//        User manager = userService.findByEmail(managerEmail)
//                .orElseThrow(() -> new RuntimeException("Manager not found"));
//
//        // Get the manager of this manager (which should be the admin)
//        User admin = manager.getManager();
//        if (admin == null) {
//            throw new IllegalStateException("Admin not found for this manager.");
//        }
//
//        LeaveBalance leaveBalance = leaveBalanceRepository.findByUser(manager)
//                .orElseGet(() -> createNewLeaveBalance(manager));
//
//        // Calculate effective leave days
//        long effectiveLeaveDays = calculateEffectiveLeaveDays(startDate, endDate);
//
//        // Fetch pending leave requests and calculate effective pending leave days
//        List<LeaveRequest> pendingRequests = leaveRequestRepository
//                .findByUserAndLeaveTypeAndStatus(manager, leaveType, LeaveStatus.PENDING);
//
//        long totalPendingLeaveDays = pendingRequests.stream()
//                .mapToLong(req -> calculateEffectiveLeaveDays(req.getStartDate(), req.getEndDate()))
//                .sum();
//
//        // Validate leave balance
//        validateLeaveBalance(leaveBalance, leaveType, effectiveLeaveDays, totalPendingLeaveDays);
//
//        // Create and save leave request
//        LeaveRequest leaveRequest = LeaveRequest.builder()
//                .user(manager)
//                .manager(admin)  // Assigning the manager of this manager (admin)
//                .startDate(startDate)
//                .endDate(endDate)
//                .leaveType(leaveType)
//                .status(LeaveStatus.PENDING)
//                .build();
//
//        leaveRequestRepository.save(leaveRequest);
//    }


//    @Transactional
//    public void applyLeave(String managerEmail, LocalDate startDate, LocalDate endDate, LeaveType leaveType) {
//        LocalDate tomorrow = LocalDate.now().plusDays(1); // Ensure start date is at least tomorrow
//
//        // Ensure startDate is tomorrow or in the future
//        if (startDate.isBefore(tomorrow)) {
//            throw new IllegalArgumentException("Start date must be tomorrow or a future date.");
//        }
//
//        // Ensure endDate is equal to or greater than startDate
//        if (endDate.isBefore(startDate)) {
//            throw new IllegalArgumentException("End date must be equal to or greater than start date.");
//        }
//
//        User manager = userRepository.findByEmail(managerEmail)
//                .orElseThrow(() -> new RuntimeException("Manager not found"));
//
//        // Get the manager of this manager (which should be the admin)
//        User admin = manager.getManager();
//        if (admin == null) {
//            throw new IllegalStateException("Admin not found for this manager.");
//        }
//
//        // Validate leave balance including pending leaves and holidays
//        validateLeaveBalance(manager, leaveType, startDate, endDate);
//
//        // Create and save the leave request
//        LeaveRequest leaveRequest = LeaveRequest.builder()
//                .user(manager)
//                .manager(admin) // Assigning the manager of this manager (admin)
//                .startDate(startDate)
//                .endDate(endDate)
//                .leaveType(leaveType)
//                .status(LeaveStatus.PENDING)
//                .build();
//
//        leaveRequestRepository.save(leaveRequest);
//    }

    @Transactional
    public void applyLeave(String managerEmail, LocalDate startDate, LocalDate endDate, LeaveType leaveType) {
        LocalDate tomorrow = LocalDate.now().plusDays(1); // Ensure start date is at least tomorrow

        // Ensure startDate is tomorrow or in the future
        if (startDate.isBefore(tomorrow)) {
            throw new IllegalArgumentException("Start date must be tomorrow or a future date.");
        }

        // Ensure endDate is equal to or greater than startDate
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be equal to or greater than start date.");
        }

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Get the manager of this manager (which should be the admin)
        User admin = manager.getManager();
        if (admin == null) {
            throw new IllegalStateException("Admin not found for this manager.");
        }

        // Check if there are any overlapping leave requests (Pending or Approved)
        boolean isOverlapping = leaveRequestRepository.existsByUserAndLeaveTypeAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                manager, leaveType, List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED), endDate, startDate);

        if (isOverlapping) {
            throw new IllegalArgumentException("Leave request overlaps with existing pending or approved leave requests.");
        }

        // Validate leave balance including pending leaves and holidays
        validateLeaveBalance(manager, leaveType, startDate, endDate);

        // Create and save the leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .user(manager)
                .manager(admin) // Assigning the manager of this manager (admin)
                .startDate(startDate)
                .endDate(endDate)
                .leaveType(leaveType)
                .status(LeaveStatus.PENDING)
                .build();

        leaveRequestRepository.save(leaveRequest);

        log.info("going to send mail request to email service from leave request service ");
        emailService.sendLeaveRequestEmail(
                admin.getEmail(),
                manager.getFirstName() + " " + manager.getLastName(),
                startDate.toString(),
                endDate.toString(),
                leaveType.toString()
        );
        log.info("Email request send successfully , check email of admin");

    }


//    @Transactional
//    public void applyLeave(String managerEmail, LocalDate startDate, LocalDate endDate, LeaveType leaveType) {
//        if (endDate.isBefore(startDate)) {
//            throw new IllegalArgumentException("End date must be equal to or greater than start date.");
//        }
//        User manager = userRepository.findByEmail(managerEmail)
//                .orElseThrow(() -> new RuntimeException("Manager not found"));
//
//        // Get the manager of this manager (which should be the admin)
//        User admin = manager.getManager();
//        if (admin == null) {
//            throw new IllegalStateException("Admin not found for this manager.");
//        }
//
//        // Validate leave balance including pending leaves and holidays
//        validateLeaveBalance(manager, leaveType, startDate, endDate);
//
//        // Create and save the leave request
//        LeaveRequest leaveRequest = LeaveRequest.builder()
//                .user(manager)
//                .manager(admin) // Assigning the manager of this manager (admin)
//                .startDate(startDate)
//                .endDate(endDate)
//                .leaveType(leaveType)
//                .status(LeaveStatus.PENDING)
//                .build();
//
//        leaveRequestRepository.save(leaveRequest);
//    }




    public LeaveBalance createNewLeaveBalance(User user) {
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setUser(user);
        leaveBalance.setRemainingLeaves(0);
        leaveBalance.setSickLeaves(0);
        leaveBalance.setCasualLeaves(0);
        leaveBalance.setUnpaidLeaves(0);
        leaveBalance.setPaidLeaves(0);
        leaveBalanceRepository.save(leaveBalance);
        return leaveBalance;
    }

    public void validateLeaveBalance(User user, LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        LeaveBalance leaveBalance = leaveBalanceRepository.findByUser(user)
                .orElseGet(() -> createNewLeaveBalance(user));

        long effectiveLeaveDays = calculateEffectiveLeaveDays(startDate, endDate);
        long totalPendingLeaveDays = calculatePendingLeaveDays(user, leaveType);

        long totalRequestedDays = totalPendingLeaveDays + effectiveLeaveDays;

        int remainingLeaves = leaveBalance.getRemainingLeaves();
        int leaveCategoryBalance = getLeaveCategoryBalance(leaveBalance, leaveType);

        if (leaveCategoryBalance < totalRequestedDays) {
            throw new IllegalStateException("Not enough " + leaveType + " leaves available.");
        }

        if (remainingLeaves < totalRequestedDays) {
            throw new IllegalStateException("Not enough total leaves available.");
        }
    }

    private long calculateEffectiveLeaveDays(LocalDate startDate, LocalDate endDate) {
        List<Holiday> holidays = holidayRepository.findByDateBetween(startDate, endDate);
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return totalDays - holidays.size();
    }

    private long calculatePendingLeaveDays(User user, LeaveType leaveType) {
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByUserAndLeaveTypeAndStatus(user, leaveType, LeaveStatus.PENDING);

        return pendingRequests.stream()
                .mapToLong(request -> calculateEffectiveLeaveDays(request.getStartDate(), request.getEndDate()))
                .sum();
    }

    private int getLeaveCategoryBalance(LeaveBalance leaveBalance, LeaveType leaveType) {
        if (leaveType == LeaveType.SICK) {
            return leaveBalance.getSickLeaves();
        } else if (leaveType == LeaveType.CASUAL) {
            return leaveBalance.getCasualLeaves();
        } else if (leaveType == LeaveType.UNPAID) {
            return leaveBalance.getUnpaidLeaves();
        } else if (leaveType == LeaveType.PAID) {
            return leaveBalance.getPaidLeaves();
        } else {
            throw new IllegalArgumentException("Invalid leave type: " + leaveType);
        }
    }

    @Transactional
    public void deletePendingLeaveRequest(String managerEmail, Long leaveRequestId) {
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        // Ensure the leave request belongs to the manager and is still pending
        if (!leaveRequest.getUser().equals(manager)) {
            throw new IllegalArgumentException("You can only delete your own leave requests.");
        }

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave requests can be deleted.");
        }

        leaveRequestRepository.delete(leaveRequest);
    }

    public List<LeaveRequest> getPendingLeaveRequestsByUser(String email) {
        User employee = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveRequestRepository.findByUserAndStatus(employee, LeaveStatus.PENDING);
    }

    public LeaveRequest getLeaveRequestById(Long leaveRequestId, String email) {
        User employee = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveRequestRepository.findByIdAndUser(leaveRequestId, employee)
                .orElseThrow(() -> new RuntimeException("Leave request not found or unauthorized access"));
    }

    public void deletePendingLeaveRequestEmp(Long leaveRequestId, String email) {
        User employee = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndUser(leaveRequestId, employee)
                .orElseThrow(() -> new RuntimeException("Leave request not found or unauthorized access"));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Cannot delete leave request. It is already " + leaveRequest.getStatus().toString().toLowerCase());
        }

        leaveRequestRepository.deleteById(leaveRequestId);
    }


    @Transactional
    public void applyLeaveToManager(String employeeEmail, LocalDate startDate, LocalDate endDate, LeaveType leaveType) {
        LocalDate tomorrow = LocalDate.now().plusDays(1); // Ensure leave starts tomorrow or later

        if (startDate.isBefore(tomorrow)) {
            throw new IllegalArgumentException("Start date must be tomorrow or a future date.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be equal to or greater than start date.");
        }

        // Fetch employee from DB
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Fetch manager of the employee
        User manager = employee.getManager();
        if (manager == null) {
            throw new IllegalStateException("Manager not assigned for this employee.");
        }

        // Check for overlapping leave requests (Pending or Approved)
        boolean isOverlapping = leaveRequestRepository.existsByUserAndLeaveTypeAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                employee, leaveType, List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED), endDate, startDate);

        if (isOverlapping) {
            throw new IllegalArgumentException("Leave request overlaps with existing pending or approved leave requests.");
        }

        // Validate leave balance
        validateLeaveBalance(employee, leaveType, startDate, endDate);

        // Create and save the leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .user(employee)
                .manager(manager)
                .startDate(startDate)
                .endDate(endDate)
                .leaveType(leaveType)
                .status(LeaveStatus.PENDING)
                .build();

        leaveRequestRepository.save(leaveRequest);

        log.info("going to send mail request to email service from leave request service.");
        emailService.sendLeaveRequestEmail(
                manager.getEmail(),
                employee.getFirstName() + " " + employee.getLastName(),
                startDate.toString(),
                endDate.toString(),
                leaveType.toString()
        );
        log.info("Email request send successfully , check email of admin.");
    }




}
