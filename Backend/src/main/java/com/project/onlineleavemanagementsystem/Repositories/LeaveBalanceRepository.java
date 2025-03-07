package com.project.onlineleavemanagementsystem.Repositories;

import com.project.onlineleavemanagementsystem.Entities.LeaveBalance;
import com.project.onlineleavemanagementsystem.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, String> {
    Optional<LeaveBalance> findByUser(User user);
    Optional<LeaveBalance> findByUserId(Long userId);

}
