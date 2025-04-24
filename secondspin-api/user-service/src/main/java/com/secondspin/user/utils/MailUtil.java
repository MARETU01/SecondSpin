package com.secondspin.user.utils;

import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailUtil {

    private final JavaMailSender javaMailSender;

    public MailUtil(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendVerificationCodeMail(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("SecondSpin <20223802051@m.scnu.edu.cn>");
        message.setTo(toEmail);
        message.setSubject("Your Verification Code");
        message.setText("Dear User,\n\n" +
                "Thank you for using our services! To verify your email address, please enter the verification code below within the next 10 minutes:\n\n" +
                "Verification Code: " + verificationCode + "\n\n" +
                "Please note that this verification code is valid only for this session and must be used within 10 minutes. If you do not complete the verification within 10 minutes or if this was not a request from you, please ignore this email and request a new code.\n\n" +
                "If you have any questions or need assistance, please reply to this email.\n\n" +
                "Best regards,\n" +
                "SecondSpin");

        javaMailSender.send(message);
    }
}
