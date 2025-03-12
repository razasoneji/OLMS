package com.project.onlineleavemanagementsystem.Controllers;

import com.project.onlineleavemanagementsystem.Entities.*;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import com.project.onlineleavemanagementsystem.Services.HolidayService;
import com.project.onlineleavemanagementsystem.Services.LeaveRequestService;
import com.project.onlineleavemanagementsystem.Services.ManagerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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



    @PostMapping("/employees")
    public ResponseEntity<User> createEmployee(@RequestBody CreateEmployeeRequest request, Authentication authentication) {
        User createdEmployee = managerService.createEmployee(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long employeeId) {
        managerService.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted successfully");
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


    @GetMapping("/employees/count")
    public ResponseEntity<Long> getTotalEmployeesUnderManager(Authentication authentication) {
        User manager = (User) authentication.getPrincipal(); // Get logged-in manager
        long count = managerService.getTotalEmployeesUnderManager(manager.getId());
        return ResponseEntity.ok(count);    
    }

    @PutMapping("/employees/{employeeId}/increase-credits")
    public ResponseEntity<String> increaseEmployeeCredits(@PathVariable Long employeeId,
                                                          @RequestParam int credits) {
        managerService.increaseEmployeeCredits(employeeId, credits);
        return ResponseEntity.ok("Employee credits increased successfully.");
    }


    @GetMapping("/employees/{employeeId}/leave-balance")
    public ResponseEntity<LeaveBalance> getEmployeeLeaveBalance(@PathVariable Long employeeId) {
        LeaveBalance leaveBalance = managerService.getEmployeeLeaveBalance(employeeId);
        return ResponseEntity.ok(leaveBalance);
    }

    @GetMapping("/leave-requests/approved/self")
    public ResponseEntity<List<LeaveRequest>> getApprovedLeaveRequestsByManager(Authentication authentication) {
        User manager = (User) authentication.getPrincipal();
        List<LeaveRequest> approvedRequests = leaveRequestService.getLeaveRequestsByUserAndStatus(manager.getId(), LeaveStatus.APPROVED);
        return ResponseEntity.ok(approvedRequests);
    }

    @GetMapping("/leave-requests/rejected/self")
    public ResponseEntity<List<LeaveRequest>> getRejectedLeaveRequestsByManager(Authentication authentication) {
        User manager = (User) authentication.getPrincipal();
        List<LeaveRequest> rejectedRequests = leaveRequestService.getLeaveRequestsByUserAndStatus(manager.getId(), LeaveStatus.REJECTED);
        return ResponseEntity.ok(rejectedRequests);
    }

    @GetMapping("/leave-requests/pending/self")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaveRequestsByManager(Authentication authentication) {
        User manager = (User) authentication.getPrincipal();
        List<LeaveRequest> pendingRequests = leaveRequestService.getLeaveRequestsByUserAndStatus(manager.getId(), LeaveStatus.PENDING);
        return ResponseEntity.ok(pendingRequests);
    }

    @PostMapping("/apply-leave")
    public ResponseEntity<String> applyLeave(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam LeaveType leaveType) {

        String managerEmail = authentication.getName(); // Extract email from authenticated user

        leaveRequestService.applyLeave(managerEmail, startDate, endDate, leaveType);
        return ResponseEntity.ok("Leave request submitted successfully.");
    }

    @DeleteMapping("/delete-leave/{leaveRequestId}")
    public ResponseEntity<String> deletePendingLeaveRequest(@PathVariable Long leaveRequestId, Authentication authentication) {
        String managerEmail = authentication.getName(); // Get logged-in user's email
        leaveRequestService.deletePendingLeaveRequest(managerEmail, leaveRequestId);
        return ResponseEntity.ok("Leave request deleted successfully.");
    }

    @GetMapping("/leave-balance/self")
    public ResponseEntity<LeaveBalance> getMyLeaveBalance(Authentication authentication) {
        User manager = (User) authentication.getPrincipal(); // Get logged-in manager
        LeaveBalance leaveBalance = managerService.getEmployeeLeaveBalance(manager.getId());
        return ResponseEntity.ok(leaveBalance);
    }










}
