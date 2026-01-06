package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.PaymentMethodVM;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PaymentMethodService {

    private final AppSettingService settings;
    private final ObjectMapper objectMapper;

    public PaymentMethodService(AppSettingService settings, ObjectMapper objectMapper) {
        this.settings = settings;
        this.objectMapper = objectMapper;
    }

    public List<PaymentMethodVM> listActiveMethods() {
        List<PaymentMethodVM> methods = new ArrayList<>();

        boolean pixAtivo = settings.getBoolean("pg.pix_ativo", true);
        boolean cartaoAtivo = settings.getBoolean("pg.cartao_ativo", true);
        boolean boletoAtivo = settings.getBoolean("pg.boleto_ativo", false);
        boolean dinheiroAtivo = settings.getBoolean("pg.dinheiro_ativo", false);

        if (pixAtivo) {
            methods.add(new PaymentMethodVM("pix", "PIX (recomendado)", "online"));
        }
        if (boletoAtivo) {
            methods.add(new PaymentMethodVM("boleto", "Boleto Bancario", "online"));
        }
        if (cartaoAtivo) {
            methods.add(new PaymentMethodVM("credito", "Cartao de Credito", "online"));
            methods.add(new PaymentMethodVM("debito", "Cartao de Debito", "online"));
        }
        if (dinheiroAtivo) {
            methods.add(new PaymentMethodVM("dinheiro", "Dinheiro na entrega", "offline"));
        }

        methods.addAll(loadCustomPaymentMethods());
        return methods;
    }

    public boolean isActiveValue(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (PaymentMethodVM method : listActiveMethods()) {
            if (value.equals(method.value())) {
                return true;
            }
        }
        return false;
    }

    public String resolveLabel(String value) {
        if (value == null) return "";
        for (PaymentMethodVM method : listActiveMethods()) {
            if (value.equals(method.value())) {
                return method.label();
            }
        }
        return value;
    }

    private List<PaymentMethodVM> loadCustomPaymentMethods() {
        String raw = settings.getOrDefault("pg.custom_methods", "[]");
        List<PaymentMethodVM> out = new ArrayList<>();
        try {
            List<CustomPaymentMethod> list = objectMapper.readValue(raw, new TypeReference<List<CustomPaymentMethod>>() {});
            if (list == null) {
                return out;
            }
            for (CustomPaymentMethod m : list) {
                if (m == null || !m.isAtivo()) {
                    continue;
                }
                String label = m.getNome();
                if (m.getTaxa() != null && m.getTaxa().compareTo(BigDecimal.ZERO) > 0) {
                    label = label + " (" + m.getTaxa().toPlainString() + "%)";
                }
                String value = "custom:" + (m.getId() == null ? m.getNome().toLowerCase() : m.getId());
                out.add(new PaymentMethodVM(value, label, m.getTipo()));
            }
        } catch (Exception ex) {
            return out;
        }
        return out;
    }

    private static class CustomPaymentMethod {
        private String id;
        private String nome;
        private String tipo;
        private BigDecimal taxa;
        private boolean ativo;

        public String getId() {
            return id;
        }

        @SuppressWarnings("unused")
        public void setId(String id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        @SuppressWarnings("unused")
        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getTipo() {
            return tipo;
        }

        @SuppressWarnings("unused")
        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public BigDecimal getTaxa() {
            return taxa;
        }

        @SuppressWarnings("unused")
        public void setTaxa(BigDecimal taxa) {
            this.taxa = taxa;
        }

        public boolean isAtivo() {
            return ativo;
        }

        @SuppressWarnings("unused")
        public void setAtivo(boolean ativo) {
            this.ativo = ativo;
        }
    }
}
