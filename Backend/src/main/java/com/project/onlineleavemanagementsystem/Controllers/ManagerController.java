package com.project.onlineleavemanagementsystem.Controllers;

import com.project.onlineleavemanagementsystem.Entities.*;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import com.project.onlineleavemanagementsystem.Services.HolidayService;
import com.project.onlineleavemanagementsystem.Services.LeaveRequestService;
import com.project.onlineleavemanagementsystem.Services.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    @Autowired
    private final HolidayService holidayService;

    @Autowired
    private final ManagerService managerService;

    @Autowired
    private final LeaveRequestService leaveRequestService;

    @Autowired
    private final UserRepository userRepository;


    @GetMapping("/admin")
    public ResponseEntity<User> getAdminOfManager(Authentication authentication) {
        String managerEmail = authentication.getName(); // Get the logged-in manager's email
        User admin = managerService.getAdminByManager(managerEmail);
        return ResponseEntity.ok(admin);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentManager(Authentication authentication) {
        String managerEmail = authentication.getName(); // Get the logged-in manager's email
        User manager = managerService.getCurrentManager(managerEmail);
        return ResponseEntity.ok(manager);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<String> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {

        String managerEmail = authentication.getName(); // Get the logged-in manager's email
        boolean updated = managerService.updateProfile(managerEmail, request);

        if (updated) {
            return ResponseEntity.ok("Profile updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("No valid fields provided for update.");
        }
    }


    @GetMapping("/employees")
    public ResponseEntity<List<User>> getEmployeesUnderManager(Authentication authentication) {
        String managerEmail = authentication.getName(); // Get the logged-in manager's email
        List<User> employees = managerService.getEmployeesUnderManager(managerEmail);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<Holiday>> getAllHolidays() {
        List<Holiday> holidays = holidayService.getAllHolidays();
        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<User> getEmployeeById(@PathVariable Long employeeId) {
        User employee = managerService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/employee")
    public ResponseEntity<User> createEmployee(@RequestBody User employee, Authentication authentication) {
        User createdEmployee = managerService.createEmployee(employee, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @DeleteMapping("/employee/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long employeeId) {
        managerService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted successfully.");
    }




    @GetMapping("/leave-requests/summary")
    public ResponseEntity<Map<String, Long>> getLeaveRequestSummaryForEmployees(Authentication authentication) {
        // Extract the logged-in user's email from the authentication object
        String email = authentication.getName();

        // Fetch the manager from the database using the email
        User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        Map<String, Long> summary = leaveRequestService.getLeaveRequestSummaryByManager(manager.getId());

        return ResponseEntity.ok(summary);
    }


    @GetMapping("/employees/search-by-email")
    public ResponseEntity<List<User>> searchEmployeesByEmail(
            @RequestParam String email,
            Authentication authentication) {

        // Get the logged-in manager's email from authentication
        String managerEmail = authentication.getName();

        // Fetch manager from repository
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Fetch employees under this manager whose email matches the query
        List<User> employees = managerService.findEmployeesByEmailAndManager(email, manager);

        return ResponseEntity.ok(employees);
    }



    @GetMapping("/leave-requests/rejected")
    public ResponseEntity<List<LeaveRequest>> getRejectedLeaveRequestsForManager(Authentication authentication) {
        User manager = managerService.getAuthenticatedManager(authentication);
        List<LeaveRequest> rejectedRequests = leaveRequestService.getLeaveRequestsByManagerAndStatus(manager.getId(), LeaveStatus.REJECTED);
        return ResponseEntity.ok(rejectedRequests);
    }

    @GetMapping("/leave-requests/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaveRequestsForManager(Authentication authentication) {
        User manager = managerService.getAuthenticatedManager(authentication);
        List<LeaveRequest> pendingRequests = leaveRequestService.getLeaveRequestsByManagerAndStatus(manager.getId(), LeaveStatus.PENDING);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/leave-requests/approved")
    public ResponseEntity<List<LeaveRequest>> getApprovedLeaveRequestsForManager(Authentication authentication) {
        User manager = managerService.getAuthenticatedManager(authentication);
        List<LeaveRequest> approvedRequests = managerService.getApprovedLeaveRequestsForManager(manager.getId());
        return ResponseEntity.ok(approvedRequests);
    }

    @PutMapping("/leave-requests/{requestId}/approve")
    public ResponseEntity<String> approveLeaveRequest(@PathVariable Long requestId) {
        managerService.approveLeaveRequest(requestId);
        return ResponseEntity.ok("Leave request approved successfully.");
    }

    @PutMapping("/leave-requests/{requestId}/reject")
    public ResponseEntity<String> rejectLeaveRequest(@PathVariable Long requestId) {
        managerService.rejectLeaveRequest(requestId);
        return ResponseEntity.ok("Leave request rejected successfully.");
    }

}
