package com.bytesfield.schedula.services.providers;

import com.bytesfield.schedula.models.SendEmailData;

public interface EmailProvider {
    void sendEmail(SendEmailData data);
}
