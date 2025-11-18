package com.littlebook.auth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FirebaseConfig {

  @Value("${app.firebase.credentials-path:}")
  private String credentialsPath;

  @Value("${app.firebase.project-id:}")
  private String projectId;

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      var optsBuilder = FirebaseOptions.builder();

      // 1) Credentials : fichier si présent, sinon credentials par défaut (GCP/ENV)
      if (credentialsPath != null && !credentialsPath.isBlank() && Files.exists(Path.of(credentialsPath))) {
        try (var in = new FileInputStream(credentialsPath)) {
          optsBuilder.setCredentials(GoogleCredentials.fromStream(in));
        }
      } else {
        optsBuilder.setCredentials(GoogleCredentials.getApplicationDefault());
      }

      // 2) Project ID si fourni
      if (projectId != null && !projectId.isBlank()) {
        optsBuilder.setProjectId(projectId);
      }

      FirebaseApp.initializeApp(optsBuilder.build());
    }
    return FirebaseApp.getInstance();
  }

  @Bean
  public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
    // Bean injecté (et mockable en test)
    return FirebaseAuth.getInstance(firebaseApp);
  }
}
