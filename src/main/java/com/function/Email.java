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
        String from = "noreplyproyectduoc@gmail.com";
        String password = "ifvb xyed vogf ytjy";

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

            String htmlBody = "<!DOCTYPE html>\r\n" +
                            "<html>\r\n" +
                            "<head>\r\n" +
                            "  <meta charset=\"UTF-8\">\r\n" +
                            "  <title>Solicitud Recibida</title>\r\n" +
                            "</head>\r\n" +
                            "<body style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; background-color: #f9f9f9; padding: 20px;\">\r\n" +
                            "  <div style=\"max-width: 600px; margin: auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);\">\r\n" +
                            "    <h2 style=\"text-align: center; color: #004B8D;\">✉️ Solicitud Recibida</h2>\r\n" +
                            "    <p>Estimado(a) ciudadano(a),</p>\r\n" +
                            "    <p>Hemos recibido exitosamente su solicitud ingresada en el sistema OIRS del Ministerio de Vivienda y Urbanismo (MINVU).</p>\r\n" +
                            "    <p><strong>Número de Solicitud:</strong> <span style=\"color: #004B8D;\">{{numeroSolicitud}}</span></p>\r\n" +
                            "    <p><strong>Correo asociado:</strong> {{correo}}</p>\r\n" +
                            "    <p>Nos pondremos en contacto contigo a la brevedad para entregarte una respuesta o actualización sobre tu requerimiento.</p>\r\n" +
                            "    <p>Gracias por utilizar nuestro sistema de atención ciudadana.</p>\r\n" +
                            "    <p style=\"margin-top: 30px;\">Saludos cordiales,<br>Equipo OIRS - MINVU</p>\r\n" +
                            "    <hr>\r\n" +
                            "    <small style=\"color: #999;\">Este es un mensaje automático, por favor no respondas este correo.</small>\r\n" +
                            "  </div>\r\n" +
                            "</body>\r\n" +
                            "</html>";

                        htmlBody = htmlBody
                            .replace("{{numeroSolicitud}}", numeroSolcitud)
                            .replace("{{correo}}", to);

                        message.setContent(htmlBody, "text/html; charset=utf-8");

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
