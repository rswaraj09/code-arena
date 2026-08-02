package com.codearena.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${codearena.email.resend-api-key:}")
    private String resendApiKey;

    @Value("${codearena.email.from:CodeArena <onboarding@resend.dev>}")
    private String fromEmail;

    public EmailService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .build();
    }

    public void sendOtp(String toEmail, String code, String purposeTitle) {
        String subject = "CodeArena — " + purposeTitle;

        String htmlBody = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 500px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
                    <h2 style="color: #0f172a; margin-top: 0; font-size: 20px;">CodeArena</h2>
                    <h3 style="color: #334155; margin-bottom: 8px;">%s</h3>
                    <p style="color: #64748b; font-size: 14px; line-height: 1.5;">Use the following 6-digit code to complete your request. This code is valid for <strong>10 minutes</strong>.</p>
                    <div style="margin: 24px 0; text-align: center; background-color: #f1f5f9; padding: 16px; border-radius: 8px; font-size: 28px; font-weight: bold; letter-spacing: 6px; color: #2563eb;">
                        %s
                    </div>
                    <p style="color: #94a3b8; font-size: 12px; margin-bottom: 0;">If you did not request this code, please ignore this email.</p>
                </div>
                """.formatted(purposeTitle, code);

        sendEmail(toEmail, subject, htmlBody);
    }

    public void sendPlain(String toEmail, String subject, String body) {
        String htmlBody = "<div style=\"font-family: sans-serif; padding: 16px;\"><p>" + body + "</p></div>";
        sendEmail(toEmail, subject, htmlBody);
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        log.info("[EMAIL SERVICE] OTP/Notification for {} | Subject: {}", toEmail, subject);

        if (!StringUtils.hasText(resendApiKey)) {
            log.warn("[RESEND API] RESEND_API_KEY is not configured in .env. Logging email output to console.");
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "from", fromEmail,
                    "to", List.of(toEmail),
                    "subject", subject,
                    "html", htmlContent
            );

            String response = restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            log.info("[RESEND API SUCCESS] Email sent to {}. Response: {}", toEmail, response);
        } catch (Exception ex) {
            log.error("[RESEND API ERROR] Failed to send email to {}: {}", toEmail, ex.getMessage(), ex);
        }
    }
}
