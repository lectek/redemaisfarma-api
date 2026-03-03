package br.com.redemaisfarma.adapters.inbound.web.controller.publico;

import br.com.redemaisfarma.application.service.ai.AiAssistantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("!test")
@RestController
@RequestMapping("/api/ia")
public class IaPublicController {

    /**
     * Maximum accepted customer message length.
     */
    private static final int MAX_MESSAGE_LENGTH = 2000;

    /**
     * Service that answers customer questions.
     */
    private final AiAssistantService ai;

    /**
     * Creates IA public controller.
     *
     * @param service IA assistant service
     */
    public IaPublicController(final AiAssistantService service) {
        this.ai = service;
    }

    /**
     * Receives customer prompt and returns IA response.
     *
     * @param request ask payload
     * @param http servlet request
     * @return answer with resolved session id
     */
    @PostMapping(
            value = "/ask",
            consumes = "application/json",
            produces = "application/json"
    )
    public AskResponse ask(
            @RequestBody final AskRequest request,
            final HttpServletRequest http
    ) {
        final String sid = request.sessionId() != null
                && !request.sessionId().isBlank()
                ? request.sessionId()
                : safeClientSession(http);

        String msg = request.message() == null ? "" : request.message().trim();
        if (msg.length() > MAX_MESSAGE_LENGTH) {
            msg = msg.substring(0, MAX_MESSAGE_LENGTH);
        }

        final String answer = ai.answer(sid, msg);
        return new AskResponse(sid, answer);
    }

    private String safeClientSession(final HttpServletRequest http) {
        final String ua = String.valueOf(http.getHeader("User-Agent"));
        final String ip = String.valueOf(http.getRemoteAddr());
        return UUID.nameUUIDFromBytes((ua + "|" + ip).getBytes()).toString();
    }

    public record AskRequest(@NotBlank String message, String sessionId) { }

    public record AskResponse(String sessionId, String answer) { }
}
