package com.ngo.ngoapp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase.config-path}")
    private String firebaseConfigPath;

    private boolean initialized = false;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Attempting to initialize Firebase Admin SDK with file: {}", firebaseConfigPath);
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(firebaseConfigPath);

            if (serviceAccount == null) {
                log.warn("Firebase credential file '{}' not found in classpath. Firebase will run in MOCK MODE for local testing.", firebaseConfigPath);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            initialized = true;
            log.info("Firebase Admin SDK initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK: {}. Firebase will run in MOCK MODE for local testing.", e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}
