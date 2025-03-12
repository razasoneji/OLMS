//package com.project.onlineleavemanagementsystem.Controllers;
//
//public class AdminController {
//}

package com.project.onlineleavemanagementsystem.Controllers;

import com.project.onlineleavemanagementsystem.Entities.*;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import com.project.onlineleavemanagementsystem.Services.AdminService;
import com.project.onlineleavemanagementsystem.Services.HolidayService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/{id}")
    public ResponseEntity<User> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @GetMapping("/managers")
    public ResponseEntity<List<User>> getAllManagers() {
        return ResponseEntity.ok(adminService.getAllManagers());
    }

    @GetMapping("/manager/{id}")
    public ResponseEntity<User> getManagerById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getManagerById(id));
    }

    @PostMapping("/manager")
    public ResponseEntity<User> createManager(@RequestBody User manager) {
        return ResponseEntity.ok(adminService.createManager(manager));
    }

    @DeleteMapping("/manager/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable Long id) {
        adminService.deleteManager(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/leave-requests/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaveRequests() {
        return ResponseEntity.ok(adminService.getLeaveRequestsByStatus(LeaveStatus.PENDING));
    }

    @GetMapping("/leave-requests/approved")
    public ResponseEntity<List<LeaveRequest>> getApprovedLeaveRequests() {
        return ResponseEntity.ok(adminService.getLeaveRequestsByStatus(LeaveStatus.APPROVED));
    }

    @GetMapping("/leave-requests/rejected")
    public ResponseEntity<List<LeaveRequest>> getRejectedLeaveRequests() {
        return ResponseEntity.ok(adminService.getLeaveRequestsByStatus(LeaveStatus.REJECTED));
    }



//    @PatchMapping("/leave-requests/{leaveRequestId}/status")
//    public ResponseEntity<String> updateLeaveRequestStatus(
//            @PathVariable Long leaveRequestId,
//            @RequestParam LeaveStatus status) {
//
//        boolean updated = adminService.updateLeaveRequestStatus(leaveRequestId, status);
//
//        if (updated) {
//            return ResponseEntity.ok("Leave request status updated successfully.");
//        } else {
//            return ResponseEntity.badRequest().body("Leave request is not in PENDING state or does not exist.");
//        }
//    }

    // working approve reject endpoint.
    @PatchMapping("/leave-requests/{leaveRequestId}/status")
    public ResponseEntity<String> updateLeaveRequestStatus(
            @PathVariable Long leaveRequestId,
            @RequestParam LeaveStatus status) {

        if (status != LeaveStatus.APPROVED && status != LeaveStatus.REJECTED) {
            return ResponseEntity.badRequest().body("Invalid status. Only APPROVED or REJECTED is allowed.");
        }

        boolean updated = adminService.updateManagerLeaveStatus(leaveRequestId, status);

        if (updated) {
            return ResponseEntity.ok("Leave request status updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("Leave request is not in PENDING state, not for a manager, or does not exist.");
        }
    }

    //working. get employee's leave balance
    @GetMapping("/employees/{employeeId}/leave-balance")
    public ResponseEntity<LeaveBalance> getEmployeeLeaveBalance(@PathVariable Long employeeId) {
        LeaveBalance leaveBalance = adminService.getEmployeeLeaveBalance(employeeId);

        if (leaveBalance != null) {
            return ResponseEntity.ok(leaveBalance);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    // search employee by email like

    @GetMapping("/managers/search-by-email")
    public ResponseEntity<List<User>> searchManagersByEmail(@RequestParam String email) {
        List<User> managers = adminService.searchManagersByEmail(email);
        return ResponseEntity.ok(managers);
    }


    @GetMapping("/holidays")
    public ResponseEntity<List<Holiday>> getAllHolidays() {
        List<Holiday> holidays = holidayService.getAllHolidays();
        return ResponseEntity.ok(holidays);
    }

    @PostMapping("/holidays")
    public ResponseEntity<Holiday> createHoliday(@RequestBody Holiday holiday) {
        Holiday createdHoliday = holidayService.createHoliday(holiday);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHoliday);
    }

    @DeleteMapping("/holidays/{holidayId}")
    public ResponseEntity<String> deleteHoliday(@PathVariable Long holidayId) {
        boolean deleted = holidayService.deleteHoliday(holidayId);
        if (deleted) {
            return ResponseEntity.ok("Holiday deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Holiday not found.");
        }
    }


    @GetMapping("/approved-leaves/managers")
    public ResponseEntity<Map<String, Integer>> getApprovedLeavesForManagers() {
        return ResponseEntity.ok(adminService.getTotalApprovedLeavesForManagers());
    }


    @GetMapping("/rejected-leaves/managers")
    public ResponseEntity<Long> getRejectedLeavesForManagers() {
        return ResponseEntity.ok(adminService.getRejectedLeavesForManagers());
    }

    @GetMapping("/pending-leaves/managers")
    public ResponseEntity<Long> getPendingLeavesForManagers() {
        return ResponseEntity.ok(adminService.getPendingLeavesForManagers());
    }


    @PatchMapping("/managers/{managerId}/increase-credits")
    public ResponseEntity<String> increaseManagerCredits(
            @PathVariable Long managerId,
            @RequestParam int creditUnits) {

        adminService.increaseManagerCredits(managerId, creditUnits);
        return ResponseEntity.ok("Credits increased successfully.");
    }


    @GetMapping("/managers/count")
    public ResponseEntity<Long> getTotalManagersCount() {
        long count = adminService.getTotalManagersCount();
        return ResponseEntity.ok(count);
    }






}
