package com.ngo.ngoapp.config;

import com.razorpay.RazorpayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class RazorpayConfig {

    private static final Logger log = LoggerFactory.getLogger(RazorpayConfig.class);

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    private RazorpayClient client;
    private boolean mockMode = false;

    @PostConstruct
    public void init() {
        if ("rzp_test_placeholder_key".equals(keyId) || keyId == null || keyId.trim().isEmpty()) {
            log.warn("Razorpay API key-id is placeholder or empty. Payments will operate in MOCK MODE.");
            this.mockMode = true;
        } else {
            try {
                this.client = new RazorpayClient(keyId, keySecret);
                log.info("Razorpay Client successfully initialized.");
            } catch (Exception e) {
                log.error("Failed to initialize Razorpay Client: {}. Falling back to MOCK MODE.", e.getMessage());
                this.mockMode = true;
            }
        }
    }

    public String getKeyId() { return keyId; }
    public String getKeySecret() { return keySecret; }
    public boolean isMockMode() { return mockMode; }

    public RazorpayClient getClient() {
        if (mockMode) return null;
        return client;
    }
}
