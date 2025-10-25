package com.clinica.gestor_citas.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.UUID;
@Service
public class DialogflowService {
    private static final String PROJECT_ID = "TU_PROJECT_ID"; // del agente
    private static final String CREDENTIALS_PATH = "src/main/resources/chatbot-key.json";

    public String enviarMensaje(String mensajeUsuario) throws Exception {
        SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(() ->
                        GoogleCredentials.fromStream(new FileInputStream(CREDENTIALS_PATH)))
                .build();

        SessionsClient sessionsClient = SessionsClient.create(sessionsSettings);
        SessionName session = SessionName.of(PROJECT_ID, UUID.randomUUID().toString());

        TextInput textInput = TextInput.newBuilder().setText(mensajeUsuario).setLanguageCode("es").build();
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

        DetectIntentRequest request = DetectIntentRequest.newBuilder()
                .setSession(session.toString())
                .setQueryInput(queryInput)
                .build();

        DetectIntentResponse response = sessionsClient.detectIntent(request);
        sessionsClient.close();

        return response.getQueryResult().getFulfillmentText(); // Respuesta del bot
    }
}
