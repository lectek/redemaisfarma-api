package br.com.redemaisfarma.adapters.inbound.web.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class LocaleChangeInterceptorConfig implements WebMvcConfigurer {
  @Override public void addInterceptors(InterceptorRegistry registry) {
    var i = new LocaleChangeInterceptor();
    i.setParamName("lang");
    registry.addInterceptor(i);
  }
}
