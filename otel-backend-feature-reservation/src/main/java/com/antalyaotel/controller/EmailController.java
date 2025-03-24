package com.antalyaotel.controller;

import com.antalyaotel.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }
    @GetMapping("/send-email")
    public String sendTestEmail(@RequestParam String to) {
        emailService.sendEmail(to, "Test Email", "Bu bir test e-postasıdır! 💌");
        return "E-posta gönderildi!";
    }
}
