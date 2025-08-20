package br.com.redemaisfarma.adapters.inbound.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Inspect Authorization header and set SecurityContext if valid (todo) */
@Component
public class JwtOncePerRequestFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    // TODO: parse and validate JWT from Authorization header, then set SecurityContext
    chain.doFilter(request, response);
  }
}
