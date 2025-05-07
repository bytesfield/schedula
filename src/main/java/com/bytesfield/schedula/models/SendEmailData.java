package com.bytesfield.schedula.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailData {
    private String recipient;                     // Email address to send to
    private String subject;                       // Subject of the email

    // Optional: For template-based emails (e.g., Thymeleaf + SMTP)
    private String templateName;                 // Template name (optional)
    private Map<String, Object> templateVariables; // Template variables (optional)

    // Optional: For direct content-based emails (e.g., Mailgun API, SendGrid API)
    private String htmlContent;                  // Raw HTML body (optional)
    private String textContent;                  // Raw plain text body (optional)

    // Optional: Attachments or other metadata can be added here later
}
