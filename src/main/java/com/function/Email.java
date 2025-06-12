package com.function;

import com.function.DTO.EmailRequest;
import com.google.gson.Gson;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Optional;
import java.util.Properties;

public class Email {

    @FunctionName("SendEmail")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {

        // Leer JSON como string
        String jsonBody = request.getBody().orElse("");

        // Deserializar usando Gson
        Gson gson = new Gson();
        EmailRequest emailRequest = gson.fromJson(jsonBody, EmailRequest.class);

        if (emailRequest == null || emailRequest.getCorreo() == null || emailRequest.getNumeroSolicitud() == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Por favor, proporciona un correo y el número de solicitud en la solicitud.")
                    .build();
        }

        String to = emailRequest.getCorreo();
        String numeroSolcitud = emailRequest.getNumeroSolicitud();
        String subject = "Solicitud recibida con éxito";
        String body = "Se ha realizado una nueva solicitud: " + numeroSolcitud;

        String host = "smtp.gmail.com";
        String from = "kotesepulveda28@gmail.com";
        String password = "ieou biui ilct wvqg";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        try {
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            context.getLogger().info("Correo enviado correctamente");

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Correo enviado correctamente")
                    .build();

        } catch (MessagingException e) {
            context.getLogger().severe("Error al enviar correo: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo")
                    .build();
        }
    }
}
