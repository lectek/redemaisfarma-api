package br.com.redemaisfarma.adapters.inbound.web.controller.publico;

import br.com.redemaisfarma.application.service.ai.AiAssistantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Profile("!test")
@RestController
@RequestMapping("/api/ia")
public class IaPublicController {
    private final AiAssistantService ai;

    public IaPublicController(AiAssistantService ai) {
        this.ai = ai;
    }

    @PostMapping(value = "/ask", consumes = "application/json", produces = "application/json")
    public AskResponse ask(@RequestBody AskRequest req, HttpServletRequest http) {
        String sid = (req.sessionId() != null && !req.sessionId().isBlank())
                ? req.sessionId()
                : safeClientSession(http);

        String msg = req.message() == null ? "" : req.message().trim();
        if (msg.length() > 2000) msg = msg.substring(0, 2000);

        String answer = ai.answer(sid, msg);
        return new AskResponse(sid, answer);
    }

    private String safeClientSession(HttpServletRequest http) {
        String ua = String.valueOf(http.getHeader("User-Agent"));
        String ip = String.valueOf(http.getRemoteAddr());
        return UUID.nameUUIDFromBytes((ua + "|" + ip).getBytes()).toString();
    }

    public record AskRequest(@NotBlank String message, String sessionId) {}
    public record AskResponse(String sessionId, String answer) {}
}
