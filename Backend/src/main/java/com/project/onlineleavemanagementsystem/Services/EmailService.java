package com.project.onlineleavemanagementsystem.Services;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}") // Your sender email from application.properties
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendLeaveRequestEmail(String managerEmail, String employeeName, String startDate, String endDate, String leaveType) {
        log.info("Inside the sendLeaveRequestEmail method of EmailService");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(managerEmail);
        message.setSubject("Leave Request Notification");
        message.setText("Dear Manager/Admin,\n\n"
                + "An Employee named " + employeeName + " has applied for leave. This Employee's leave is under your review, kindly go to the LeaveEase Website and review this request.\n"
                + "Some Details of the leave applied are as follows: \n"
                + "Leave Type: " + leaveType + "\n"
                + "Start Date: " + startDate + "\n"
                + "End Date: " + endDate + "\n\n"
                + "Please review the request, as early as possible.\n\n"
                + "Best Regards,\n\nLeaveEase - Online Leave Management System");

        mailSender.send(message);
        log.info("Leave Request Email Sent");
    }
}
