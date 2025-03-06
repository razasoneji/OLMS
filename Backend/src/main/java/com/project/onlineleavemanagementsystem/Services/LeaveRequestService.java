package com.project.onlineleavemanagementsystem.Services;


import com.project.onlineleavemanagementsystem.Entities.LeaveStatus;
import com.project.onlineleavemanagementsystem.Repositories.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;

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
}
