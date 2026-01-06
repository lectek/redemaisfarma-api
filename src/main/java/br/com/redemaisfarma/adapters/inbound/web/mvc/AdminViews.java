/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.servlet.config.annotation.ViewControllerRegistry
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
package br.com.redemaisfarma.adapters.inbound.web.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminViews
implements WebMvcConfigurer {
    public void addViewControllers(ViewControllerRegistry r) {
        r.addViewController("/admin/index").setViewName("pages/admin/index");
        r.addViewController("/admin/painel").setViewName("pages/admin/painel");
        r.addViewController("/admin/agendamentos/medicacao").setViewName("pages/admin/agendamentos/medicacao");
        r.addViewController("/admin/agendamentos/regras-desconto").setViewName("pages/admin/agendamentos/regras-desconto");
        r.addViewController("/admin/email/central").setViewName("pages/admin/email/central");
        r.addViewController("/admin/estoque/fornecedores").setViewName("pages/admin/estoque/fornecedores");
        r.addViewController("/admin/estoque/niveis").setViewName("pages/admin/estoque/niveis");
        r.addViewController("/admin/financeiro/assinaturas").setViewName("pages/admin/financeiro/assinaturas");
        r.addViewController("/admin/financeiro/assinaturas/detalhe").setViewName("pages/admin/financeiro/assinaturas-detalhe");
        r.addViewController("/admin/financeiro/gateways").setViewName("pages/admin/financeiro/gateways");
        r.addViewController("/admin/financeiro/pagamentos").setViewName("pages/admin/financeiro/pagamentos");
        r.addViewController("/admin/clientes").setViewName("pages/admin/clientes/lista");
        r.addViewController("/admin/clientes/lista").setViewName("pages/admin/clientes/lista");
        r.addViewController("/admin/marketing/notificacoes").setViewName("pages/admin/marketing/notificacoes");
        r.addViewController("/admin/produtos").setViewName("pages/admin/produtos/lista");
        r.addViewController("/admin/produtos/lista").setViewName("pages/admin/produtos/lista");
        r.addViewController("/admin/produtos/catalogo").setViewName("pages/admin/produtos/catalogo");
        r.addViewController("/admin/relatorios/clientes").setViewName("pages/admin/relatorios/clientes");
        r.addViewController("/admin/relatorios/produtos").setViewName("pages/admin/relatorios/produtos");
        r.addViewController("/admin/relatorios/vendas").setViewName("pages/admin/relatorios/vendas");
        r.addViewController("/admin/usuarios").setViewName("pages/admin/usuarios/lista");
        r.addViewController("/admin/usuarios/lista").setViewName("pages/admin/usuarios/lista");
        r.addViewController("/admin/usuarios/perfil").setViewName("pages/admin/usuarios/perfil");
        r.addViewController("/admin/usuarios/form").setViewName("pages/admin/usuarios/form");
    }
}
