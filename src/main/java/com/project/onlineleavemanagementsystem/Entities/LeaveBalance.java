package com.project.onlineleavemanagementsystem.Entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leave_balances")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Each employee has a leave balance

    private int totalLeaves = 30; // Default total leaves
    private int remainingLeaves = 30; // Initially same as totalLeaves

    private int sickLeaves = 10;
    private int casualLeaves = 5;
    private int unpaidLeaves = 5;
    private int paidLeaves = 10;


    private int credits = 0;
}
