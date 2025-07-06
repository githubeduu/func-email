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

        String jsonBody = request.getBody().orElse("");
        Gson gson = new Gson();
        EmailRequest emailRequest = gson.fromJson(jsonBody, EmailRequest.class);

        if (emailRequest == null || emailRequest.getCorreo() == null || emailRequest.getNumeroSolicitud() == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Por favor, proporciona un correo, número de solicitud y tipo en la solicitud.")
                    .build();
        }

        String to = emailRequest.getCorreo();
        String numeroSolicitud = emailRequest.getNumeroSolicitud();
        int tipo = emailRequest.getTipo();

        String subject;
        String htmlBody;

        if (tipo == 2) {
            subject = "Estado de tu solicitud actualizado";
            htmlBody = getHtmlPlantillaCambioEstado(numeroSolicitud, to, emailRequest.getDetalleMovimiento());
        } else {
            subject = "Solicitud recibida con éxito";
            htmlBody = getHtmlPlantillaRecibida(numeroSolicitud, to);
        }

        String host = "smtp.gmail.com";
        String from = "noreplyproyectduoc@gmail.com";
        String password = "bejn lkxj mnps qkpx";

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

    private static String getHtmlPlantillaRecibida(String numeroSolicitud, String correo) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Solicitud Recibida</title></head>" +
                "<body style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; background-color: #f9f9f9; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);\">" +
                "<h2 style=\"text-align: center; color: #004B8D;\">✉️ Solicitud Recibida</h2>" +
                "<p>Estimado(a) ciudadano(a),</p>" +
                "<p>Hemos recibido exitosamente su solicitud ingresada en el sistema OIRS del Ministerio de Vivienda y Urbanismo (MINVU).</p>" +
                "<p><strong>Número de Solicitud:</strong> <span style=\"color: #004B8D;\">" + numeroSolicitud + "</span></p>" +
                "<p><strong>Correo asociado:</strong> " + correo + "</p>" +
                "<p>Nos pondremos en contacto contigo a la brevedad para entregarte una respuesta o actualización sobre tu requerimiento.</p>" +
                "<p>Gracias por utilizar nuestro sistema de atención ciudadana.</p>" +
                "<p style=\"margin-top: 30px;\">Saludos cordiales,<br>Equipo OIRS - MINVU</p>" +
                "<hr><small style=\"color: #999;\">Este es un mensaje automático, por favor no respondas este correo.</small>" +
                "</div></body></html>";
    }

    private static String getHtmlPlantillaCambioEstado(String numeroSolicitud, String correo, String detalleMovimiento) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Estado Actualizado</title></head>" +
                "<body style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; background-color: #f4f4f4; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);\">" +
                "<h2 style=\"text-align: center; color: #D97706;\">📌 Estado de Solicitud Actualizado</h2>" +
                "<p>Estimado(a) ciudadano(a),</p>" +
                "<p>Te informamos que tu solicitud con número <strong style=\"color:#D97706;\">" + numeroSolicitud + "</strong> ha cambiado de estado.</p>" +
                "<p><strong>Detalle de la gestión:</strong></p>" +
                "<p style=\"background-color: #fef3c7; padding: 10px; border-left: 4px solid #D97706; border-radius: 4px;\">" +
                detalleMovimiento + "</p>" +
                "<p>Revisa tu correo regularmente para conocer más actualizaciones o respuestas.</p>" +
                "<p style=\"margin-top: 30px;\">Saludos cordiales,<br>Equipo OIRS - MINVU</p>" +
                "<hr><small style=\"color: #999;\">Este es un mensaje automático, por favor no respondas este correo.</small>" +
                "</div></body></html>";
    }
}
