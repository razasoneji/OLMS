package com.project.onlineleavemanagementsystem.Services;

import com.project.onlineleavemanagementsystem.Entities.*;
import com.project.onlineleavemanagementsystem.Repositories.HolidayRepository;
import com.project.onlineleavemanagementsystem.Repositories.LeaveBalanceRepository;
import com.project.onlineleavemanagementsystem.Repositories.LeaveRequestRepository;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final LeaveRequestService leaveRequestService;

    @Autowired
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private final LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private final HolidayRepository holidayRepository;

    private final JavaMailSender mailSender;

    public User getAdminByManager(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

        if (manager.getManager() == null || !manager.getManager().getRole().equals(Role.ADMIN)) {
            throw new EntityNotFoundException("No admin assigned to this manager");
        }

        return manager.getManager(); // Return the associated admin
    }

    public User getCurrentManager(String managerEmail) {
        return userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));
    }


    public User getEmployeeById(Long employeeId) {
        return userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
    }



    @Transactional
    public boolean updateProfile(String email, UpdateProfileRequest request) {
        User manager = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

        boolean updated = false;

        // Update first name if provided and not blank
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            manager.setFirstName(request.getFirstName().trim());
            updated = true;
        }

        // Update last name if provided and not blank
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            manager.setLastName(request.getLastName().trim());
            updated = true;
        }

        // Update password if provided and not blank
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            manager.setPassword(passwordEncoder.encode(request.getPassword().trim()));
            updated = true;
        }

        if (updated) {
            userRepository.save(manager);
        }

        return updated;
    }

    public List<User> getEmployeesUnderManager(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

        return userRepository.findByManager(manager); // Returning the whole User entity
    }





    public User findEmployeeByEmailAndManager(String email, User manager) {
        return userRepository.findByEmailAndManager(email, manager)
                .orElseThrow(() -> new RuntimeException("Employee not found under this manager"));
    }

    public List<User> findEmployeesByEmailAndManager(String email, User manager) {
        return userRepository.findByEmailLikeAndManager(email, manager);
    }

    public User getAuthenticatedManager(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Manager not found"));
    }

    public List<LeaveRequest> getApprovedLeaveRequestsForManager(Long managerId) {
        return leaveRequestService.getLeaveRequestsByManagerAndStatus(managerId, LeaveStatus.APPROVED);
    }

    @Transactional
    public void approveLeaveRequest(Long requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found."));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request is not in PENDING state.");
        }

        User employee = leaveRequest.getUser();
        LeaveBalance leaveBalance = leaveBalanceRepository.findByUser(employee)
                .orElseGet(() -> createNewLeaveBalance(employee)); // If no balance exists, create one

        int totalLeaveDays = (int) ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;

        // Count holidays in the leave period
        long holidayCount = holidayRepository.countByDateBetween(leaveRequest.getStartDate(), leaveRequest.getEndDate());

        // Actual leave days excluding holidays
        int actualLeaveDays = totalLeaveDays - (int) holidayCount;

        if (actualLeaveDays > 0) {
            deductLeaves(leaveBalance, leaveRequest.getLeaveType(), actualLeaveDays);
            leaveBalanceRepository.save(leaveBalance);
        }

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public void rejectLeaveRequest(Long requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found."));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request is not in PENDING state.");
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequestRepository.save(leaveRequest);
    }

    private LeaveBalance createNewLeaveBalance(User employee) {
        LeaveBalance newBalance = LeaveBalance.builder()
                .user(employee)
                .totalLeaves(30)
                .remainingLeaves(30)
                .sickLeaves(10)
                .casualLeaves(5)
                .unpaidLeaves(5)
                .paidLeaves(10)
                .credits(0)
                .build();
        return leaveBalanceRepository.save(newBalance);
    }

    private void deductLeaves(LeaveBalance leaveBalance, LeaveType leaveType, int actualLeaveDays) {
        leaveBalance.setRemainingLeaves(leaveBalance.getRemainingLeaves() - actualLeaveDays);

        if (leaveType == LeaveType.SICK) {
            leaveBalance.setSickLeaves(Math.max(0, leaveBalance.getSickLeaves() - actualLeaveDays));
        } else if (leaveType == LeaveType.CASUAL) {
            leaveBalance.setCasualLeaves(Math.max(0, leaveBalance.getCasualLeaves() - actualLeaveDays));
        } else if (leaveType == LeaveType.UNPAID) {
            leaveBalance.setUnpaidLeaves(Math.max(0, leaveBalance.getUnpaidLeaves() - actualLeaveDays));
        } else if (leaveType == LeaveType.PAID) {
            leaveBalance.setPaidLeaves(Math.max(0, leaveBalance.getPaidLeaves() - actualLeaveDays));
        }
    }

    public long getTotalEmployeesUnderManager(Long managerId) {
        return userRepository.countByManagerId(managerId);
    }


    public void increaseEmployeeCredits(Long employeeId, int credits) {
        if (credits < 1 || credits > 5) {
            throw new IllegalArgumentException("Credits must be between 1 and 5.");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        LeaveBalance leaveBalance = leaveBalanceRepository.findByUserId(employeeId)
                .orElseGet(() -> {
                    LeaveBalance newLeaveBalance = new LeaveBalance();
                    newLeaveBalance.setUser(employee);
                    return leaveBalanceRepository.save(newLeaveBalance);
                });

        leaveBalance.setCredits(leaveBalance.getCredits() + credits);
        leaveBalance.setPaidLeaves(leaveBalance.getPaidLeaves() + credits);
        leaveBalance.setRemainingLeaves(leaveBalance.getRemainingLeaves() + credits);

        leaveBalanceRepository.save(leaveBalance);
    }

    public LeaveBalance getEmployeeLeaveBalance(Long employeeId) {
        return leaveBalanceRepository.findByUserId(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found for employee ID: " + employeeId));
    }
    //working one
//    public User createEmployee(CreateEmployeeRequest request, Authentication authentication) {
//        // Check if an employee with the same email already exists
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new IllegalArgumentException("An employee with this email already exists.");
//        }
//
//        User manager = userRepository.findByEmail(authentication.getName())
//                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));
//
//        User employee = new User();
//        employee.setFirstName(request.getFirstName());
//        employee.setLastName(request.getLastName());
//        employee.setEmail(request.getEmail());
//        employee.setPassword(passwordEncoder.encode(request.getPassword()));
//        employee.setRole(Role.EMPLOYEE);
//        employee.setManager(manager);
//        employee.setDepartment(manager.getDepartment());
//
//        User savedEmployee = userRepository.save(employee);
//
//        LeaveBalance leaveBalance = new LeaveBalance();
//        leaveBalance.setUser(savedEmployee);
//        leaveBalanceRepository.save(leaveBalance);
//
//        return savedEmployee;
//    }

    @Transactional
    public User createEmployee(CreateEmployeeRequest request, Authentication authentication) {
        // Check if an employee with the same email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An employee with this email already exists.");
        }

        User manager = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

        // Generate a random temporary password
        String tempPassword = generateTemporaryPassword();

        // Create new employee and set attributes
        User employee = new User();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPassword(passwordEncoder.encode(tempPassword)); // Store hashed password
        employee.setRole(Role.EMPLOYEE);
        employee.setManager(manager);
        employee.setDepartment(manager.getDepartment());

        // Save the employee
        User savedEmployee = userRepository.save(employee);

        // Assign leave balance to the new employee
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setUser(savedEmployee);
        leaveBalanceRepository.save(leaveBalance);

        // Send email to employee with their credentials
        sendEmployeeCredentialsEmail(savedEmployee.getEmail(), tempPassword);

        return savedEmployee;
    }

    private void sendEmployeeCredentialsEmail(String email, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Account Credentials - LeaveEase");
        message.setText("Dear Employee,\n\n" +
                "Your account has been successfully created on the Online Leave Management System.\n\n" +
                "Here are your login credentials:\n" +
                "Email: " + email + "\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "Please log in and change your password immediately.\n\n" +
                "Try to set some Strong password which is non-obvious.\n\n" +
                "Best Regards,\n" +
                "LeaveEase Team");

        mailSender.send(message);
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8); // Generate an 8-character random password
    }


    public void deleteEmployee(Long employeeId) {
        if (!userRepository.existsById(employeeId)) {
            throw new EntityNotFoundException("Employee not found");
        }
        userRepository.deleteById(employeeId);
    }









}
