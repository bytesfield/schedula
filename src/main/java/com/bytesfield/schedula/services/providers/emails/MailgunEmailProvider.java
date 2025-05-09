package com.bytesfield.schedula.services.providers.emails;

import com.bytesfield.schedula.exceptions.EmailProcessingException;
import com.bytesfield.schedula.models.SendEmailData;
import com.bytesfield.schedula.services.providers.EmailProvider;
import com.bytesfield.schedula.utils.Helper;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component("mailgun")
@RequiredArgsConstructor
public class MailgunEmailProvider implements EmailProvider {

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${email.from-address}")
    private String fromAddress;

    @Override
    public void sendEmail(SendEmailData data) {
        try {
            MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(apiKey)
                    .logLevel(Logger.Level.NONE)
                    .retryer(Retryer.NEVER_RETRY)
                    .logger(new Logger.NoOpLogger())
                    .errorDecoder(new ErrorDecoder.Default())
                    .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true))
                    .createApi(MailgunMessagesApi.class);

            Message.MessageBuilder messageBuilder = Message.builder()
                    .from(fromAddress)
                    .to(data.getRecipient())
                    .subject(data.getSubject());

            buildMessageContent(data, messageBuilder);

            Helper.retryWithBackoff(() -> {
                MessageResponse response = mailgunMessagesApi.sendMessage(domain, messageBuilder.build());

                log.info("Mailgun email sent: id={}, message={}", response.getId(), response.getMessage());

                return response;
            }, 3, 500);


        } catch (Exception e) {
            log.error("Failed to send email via Mailgun: {}", e.getMessage(), e);

            throw new EmailProcessingException("Failed to send email via Mailgun", e);
        }
    }


    private void buildMessageContent(SendEmailData data, Message.MessageBuilder builder) {
        boolean hasHtml = StringUtils.hasText(data.getHtmlContent());
        boolean hasText = StringUtils.hasText(data.getTextContent());

        if (!hasHtml && !hasText) {
            throw new IllegalArgumentException("No email content provided: both HTML and text content are empty.");
        }

        if (hasHtml) builder.html(data.getHtmlContent());

        if (hasText) builder.text(data.getTextContent());
    }

}
