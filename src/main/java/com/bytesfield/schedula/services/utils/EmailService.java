package com.bytesfield.schedula.services.utils;

import com.bytesfield.schedula.exceptions.EmailProcessingException;
import com.bytesfield.schedula.models.SendEmailData;
import com.bytesfield.schedula.services.providers.EmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * EmailService handles the processing and sending of emails using different email providers.
 * It supports generating email content from templates and selecting a provider dynamically.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final Map<String, EmailProvider> emailProviders;
    private final TemplateEngine templateEngine;

    @Value("${email.provider}")
    private String selectedProvider;

    /**
     * Sends an email using the selected email provider.
     *
     * @param data the email data containing recipient, subject, template, and other details
     * @throws EmailProcessingException if email delivery fails
     * @throws IllegalArgumentException if the selected provider is invalid or not configured
     */
    public void sendEmail(SendEmailData data) {
        try {
            Context context = new Context();
            context.setVariables(data.getTemplateVariables());

            String htmlContent = getEmailContent(data, context);
            data.setHtmlContent(htmlContent);

            EmailProvider provider = emailProviders.get(selectedProvider);

            if (provider == null || !provider.getClass().isAnnotationPresent(Component.class)) {
                throw new IllegalArgumentException("Invalid email provider");
            }

            provider.sendEmail(data);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);

            // Wrap and rethrow the exception
            throw new EmailProcessingException("Email delivery failed", e);
        }
    }

    /**
     * Generates the email content using the specified template and context.
     *
     * @param data    the email data containing the template name
     * @param context the Thymeleaf context with template variables
     * @return the generated HTML content
     * @throws IllegalArgumentException if the template name is not provided
     */
    private String getEmailContent(SendEmailData data, Context context) {
        if (data.getTemplateName() == null) {
            throw new IllegalArgumentException("Template name is required for email processing");
        }

        // Process the template and return the generated content
        return templateEngine.process(data.getTemplateName(), context);
    }
}