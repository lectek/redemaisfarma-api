package br.com.redemaisfarma.infra.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthEventsLogger {

  @EventListener
  public void onSuccess(AuthenticationSuccessEvent e) {
    Authentication a = e.getAuthentication();
    String user = a.getName();
    log.info("AUTH_SUCCESS user={} details={}", user, a.getDetails());
  }

  @EventListener
  public void onFailure(AbstractAuthenticationFailureEvent e) {
    Authentication a = e.getAuthentication();
    String user = (a != null ? a.getName() : "unknown");
    String reason = e.getException() != null ? e.getException().getClass().getSimpleName() + ": " + e.getException().getMessage() : "unknown";
    log.warn("AUTH_FAIL user={} reason={} details={}", user, reason, (a != null ? a.getDetails() : null));
  }
}
