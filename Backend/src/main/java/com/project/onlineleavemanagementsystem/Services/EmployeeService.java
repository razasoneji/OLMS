package com.project.onlineleavemanagementsystem.Services;


import com.project.onlineleavemanagementsystem.Entities.*;
import com.project.onlineleavemanagementsystem.Repositories.LeaveBalanceRepository;
import com.project.onlineleavemanagementsystem.Repositories.LeaveRequestRepository;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final UserRepository userRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final HolidayService holidayService;

    // Get the currently logged-in employee
    public User getLoggedInEmployee(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    // Get the manager of the current employee
    public User getManagerOfEmployee(String email) {
        User employee = getLoggedInEmployee(email);
        return employee.getManager(); // Assuming the User entity has a "manager" field
    }

    // Update first name, last name, or password (if provided)
    public User updateEmployeeProfile(String email, String firstName, String lastName, String password) {
        User employee = getLoggedInEmployee(email);

        if (firstName != null && !firstName.isBlank()) {
            employee.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            employee.setLastName(lastName);
        }
        if (password != null && !password.isBlank()) {
            employee.setPassword(passwordEncoder.encode(password));
        }

        return userRepository.save(employee);
    }

    // Get the list of all holidays
    public List<Holiday> getAllHolidays() {
        return holidayService.getAllHolidays();
    }

    // Get number of leave requests applied by the employee
    public long getTotalLeaveRequests(String email) {
        User employee = getLoggedInEmployee(email);
        return leaveRequestRepository.countByUser(employee);
    }

    // Get number of pending, rejected, and approved leave requests
    public Map<String, Long> getLeaveRequestStatusCount(String email) {
        User employee = getLoggedInEmployee(email);

        long pending = leaveRequestRepository.countByUserAndStatus(employee, LeaveStatus.PENDING);
        long approved = leaveRequestRepository.countByUserAndStatus(employee, LeaveStatus.APPROVED);
        long rejected = leaveRequestRepository.countByUserAndStatus(employee, LeaveStatus.REJECTED);

        return Map.of("pending", pending, "approved", approved, "rejected", rejected);
    }

    // Get list of rejected leave requests
    public List<LeaveRequest> getRejectedLeaveRequests(String email) {
        User employee = getLoggedInEmployee(email);
        return leaveRequestRepository.findByUserAndStatus(employee, LeaveStatus.REJECTED);
    }

    // Get list of approved leave requests
    public List<LeaveRequest> getApprovedLeaveRequests(String email) {
        User employee = getLoggedInEmployee(email);
        return leaveRequestRepository.findByUserAndStatus(employee, LeaveStatus.APPROVED);
    }

    // Get logged-in employee's leave balance
    public LeaveBalance getEmployeeLeaveBalance(String email) {
        User employee = getLoggedInEmployee(email);
        return leaveBalanceRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Leave balance not found"));
    }
}
