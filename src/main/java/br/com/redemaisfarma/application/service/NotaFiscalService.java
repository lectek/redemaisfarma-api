// src/main/java/br/com/redemaisfarma/application/service/NotaFiscalService.java
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.NotaFiscalConfirmacaoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.NotaFiscalConfirmacaoRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class NotaFiscalService {

    private final NotaFiscalConfirmacaoRepository repo;
    private final JavaMailSender mail;

    public NotaFiscalService(NotaFiscalConfirmacaoRepository repo, JavaMailSender mail) {
        this.repo = repo;
        this.mail = mail;
    }

    @Transactional
    public Long confirmar(String nome, String preferencia, String email) {
        byte[] pdf = gerarPdf(nome, preferencia, email);

        NotaFiscalConfirmacaoEntity e = new NotaFiscalConfirmacaoEntity();
        e.setNome(nome);
        e.setPreferencia(preferencia);
        e.setEmail(email);
        e.setPdf(pdf);

        e = repo.save(e);

        // envie o e-mail; se quiser desligar em dev/test, crie um @Profile
        enviarEmail(email, "Confirmação de Nota Fiscal", "Olá " + nome + ", segue sua confirmação em anexo.", pdf,
                "confirmacao-nota-fiscal.pdf");

        return e.getId();
    }

    private static byte[] gerarPdf(String nome, String preferencia, String email) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font h1 = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font p = new Font(Font.HELVETICA, 12);

            doc.add(new Paragraph("Confirmação de Nota Fiscal", h1));
            doc.add(new Paragraph(" ", p));
            doc.add(new Paragraph("Nome: " + nome, p));
            doc.add(new Paragraph("Preferência: " + preferencia, p));
            doc.add(new Paragraph("E-mail: " + email, p));
            doc.add(new Paragraph("Gerado automaticamente pelo sistema.", p));

            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao gerar PDF", ex);
        }
    }

    private void enviarEmail(String to, String subject, String text, byte[] attachment, String filename) {
        try {
            MimeMessage msg = mail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            helper.addAttachment(filename, new ByteArrayResource(attachment));
            mail.send(msg);
        } catch (Exception e) {
            // registre e deixe seguir (ou lance exceção se for requisito)
            throw new IllegalStateException("Falha ao enviar e-mail", e);
        }
    }
}
