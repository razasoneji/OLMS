package com.project.onlineleavemanagementsystem.Services;

import com.project.onlineleavemanagementsystem.Entities.LeaveBalance;
import com.project.onlineleavemanagementsystem.Entities.Role;
import com.project.onlineleavemanagementsystem.Entities.UpdateProfileRequest;
import com.project.onlineleavemanagementsystem.Entities.User;
import com.project.onlineleavemanagementsystem.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

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

    public User createEmployee(User employee, String managerEmail) {
        // Fetch logged-in manager
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

        // Assign the employee's manager and department
        employee.setManager(manager);
        employee.setDepartment(manager.getDepartment());
        employee.setRole(Role.EMPLOYEE);

        // Assign default leave balance
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setUser(employee);
        employee.setLeaveBalance(leaveBalance);

        // Encode password before saving
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return userRepository.save(employee);
    }

    public void deleteEmployee(Long employeeId) {
        if (!userRepository.existsById(employeeId)) {
            throw new EntityNotFoundException("Employee not found with ID: " + employeeId);
        }
        userRepository.deleteById(employeeId);
    }

    public User findEmployeeByEmailAndManager(String email, User manager) {
        return userRepository.findByEmailAndManager(email, manager)
                .orElseThrow(() -> new RuntimeException("Employee not found under this manager"));
    }

    public List<User> findEmployeesByEmailAndManager(String email, User manager) {
        return userRepository.findByEmailLikeAndManager(email, manager);
    }

}
