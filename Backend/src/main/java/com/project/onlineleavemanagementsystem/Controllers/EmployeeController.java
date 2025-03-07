package com.project.onlineleavemanagementsystem.Controllers;


import com.project.onlineleavemanagementsystem.Entities.*;
import com.project.onlineleavemanagementsystem.Services.EmployeeService;
import com.project.onlineleavemanagementsystem.Services.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    private final LeaveRequestService leaveRequestService;

    // Get logged-in employee details
    @GetMapping("/me")
    public ResponseEntity<User> getLoggedInEmployee(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(employeeService.getLoggedInEmployee(email));
    }

    // Get the manager of the logged-in employee
    @GetMapping("/manager")
    public ResponseEntity<User> getManagerOfEmployee(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(employeeService.getManagerOfEmployee(email));
    }

    // Update profile details (First name, Last name, Password)
    @PutMapping("/update")
    public ResponseEntity<User> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String password) {

        String email = authentication.getName();
        return ResponseEntity.ok(employeeService.updateEmployeeProfile(email, firstName, lastName, password));
    }

    // Get the list of all holidays
    @GetMapping("/holidays")
    public ResponseEntity<List<Holiday>> getAllHolidays() {
        return ResponseEntity.ok(employeeService.getAllHolidays());
    }

    // Get the number of leave requests applied by the employee
    @GetMapping("/leave-requests/count")
    public ResponseEntity<Long> getTotalLeaveRequests(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(employeeService.getTotalLeaveRequests(email));
    }

    // Get the count of pending, approved, and rejected leave requests
    @GetMapping("/leave-requests/status")
    public ResponseEntity<Map<String, Long>> getLeaveRequestStatus(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(employeeService.getLeaveRequestStatusCount(email));
    }

    // Get list of rejected leave requests
    @GetMapping("/leave-requests/rejected")
    public ResponseEntity<List<LeaveRequest>> getRejectedLeaveRequests(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(employeeService.getRejectedLeaveRequests(email));
    }

    // Get list of approved leave requests
    @GetMapping("/leave-requests/approved")
    public ResponseEntity<List<LeaveRequest>> getApprovedLeaveRequests(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(employeeService.getApprovedLeaveRequests(email));
    }

    // Get the logged-in employee's leave balance
    @GetMapping("/leave-balance")
    public ResponseEntity<LeaveBalance> getEmployeeLeaveBalance(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(employeeService.getEmployeeLeaveBalance(email));
    }

    @GetMapping("/leave-requests/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaveRequests(Authentication authentication) {
        String email = authentication.getName(); // Get logged-in employee's email
        List<LeaveRequest> pendingRequests = leaveRequestService.getPendingLeaveRequestsByUser(email);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/leave-requests/{leaveRequestId}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(
            @PathVariable Long leaveRequestId,
            Authentication authentication) {

        String email = authentication.getName(); // Get logged-in employee's email
        LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(leaveRequestId, email);

        return ResponseEntity.ok(leaveRequest);
    }


    @DeleteMapping("/leave-requests/{leaveRequestId}")
    public ResponseEntity<String> deletePendingLeaveRequest(
            @PathVariable Long leaveRequestId,
            Authentication authentication) {

        String email = authentication.getName(); // Get logged-in employee's email
        leaveRequestService.deletePendingLeaveRequestEmp(leaveRequestId, email);

        return ResponseEntity.ok("Pending leave request deleted successfully.");
    }

    @PostMapping("/apply-leave")
    public ResponseEntity<String> applyLeave(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam LeaveType leaveType) {

        String employeeEmail = authentication.getName(); // Logged-in employee email
        leaveRequestService.applyLeaveToManager(employeeEmail, startDate, endDate, leaveType);
        return ResponseEntity.ok("Leave request submitted successfully.");
    }



}
