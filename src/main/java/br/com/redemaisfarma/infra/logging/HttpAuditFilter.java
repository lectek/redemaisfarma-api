package br.com.redemaisfarma.infra.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.MDC;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class HttpAuditFilter implements Filter {

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest http = (HttpServletRequest) req;
    HttpServletResponse httpRes = (HttpServletResponse) res;

    String path = http.getRequestURI();
    if (path.startsWith("/actuator")) {
      chain.doFilter(req, res);
      return;
    }

    // Correlation ID (reaproveita header se vier; senão cria)
    String cid = http.getHeader("X-Correlation-Id");
    if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
    MDC.put("cid", cid);

    long t0 = System.currentTimeMillis();
    ContentCachingRequestWrapper reqW = new ContentCachingRequestWrapper(http);
    ContentCachingResponseWrapper resW = new ContentCachingResponseWrapper(httpRes);

    try {
      chain.doFilter(reqW, resW);
    } finally {
      long took = System.currentTimeMillis() - t0;
      String user = SecurityContextHolder.getContext().getAuthentication() != null
          ? SecurityContextHolder.getContext().getAuthentication().getName()
          : "ANONYMOUS";
      String method = http.getMethod();
      String ip = http.getRemoteAddr();
      int status = resW.getStatus();
      int reqSize = reqW.getContentAsByteArray() != null ? reqW.getContentAsByteArray().length : 0;
      int resSize = resW.getContentSize();

      log.info("HTTP {} {} status={} user={} ip={} tookMs={} cid={} reqSize={} resSize={}",
          method, path, status, user, ip, took, cid, reqSize, resSize);

      // devolve o corpo
      resW.copyBodyToResponse();
      MDC.clear();
    }
  }
}
