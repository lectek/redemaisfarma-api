package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/permissoes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesPermissoesController {

    private static final String KEY_PERMISSOES = "permissoes.matriz";

    private final AppSettingService settings;
    private final ObjectMapper objectMapper;

    public AdminConfiguracoesPermissoesController(AppSettingService settings, ObjectMapper objectMapper) {
        this.settings = settings;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String form(Model model) {
        PermissoesForm form = new PermissoesForm();
        form.setRecursos(loadRecursos());
        model.addAttribute("form", form);
        return "pages/admin/configuracoes/permissoes";
    }

    @PostMapping
    public String salvar(@ModelAttribute("form") PermissoesForm form, RedirectAttributes ra) {
        List<RecursoPermissao> recursos = form.getRecursos() == null ? List.of() : form.getRecursos();
        try {
            String json = objectMapper.writeValueAsString(recursos);
            settings.upsert(KEY_PERMISSOES, json, "Matriz de permissoes por perfil");
            ra.addFlashAttribute("success", "Permissoes atualizadas.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Falha ao salvar permissoes.");
        }
        return "redirect:/admin/configuracoes/permissoes";
    }

    private List<RecursoPermissao> loadRecursos() {
        String raw = settings.getOrDefault(KEY_PERMISSOES, "");
        if (raw != null && !raw.isBlank()) {
            try {
                List<RecursoPermissao> list = objectMapper.readValue(raw, new TypeReference<List<RecursoPermissao>>() {});
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception ignored) {
                // fallback
            }
        }
        return defaultRecursos();
    }

    private List<RecursoPermissao> defaultRecursos() {
        List<RecursoPermissao> list = new ArrayList<>();
        list.add(new RecursoPermissao("pedidos", "Pedidos", true, true, true));
        list.add(new RecursoPermissao("produtos", "Produtos", true, true, false));
        list.add(new RecursoPermissao("clientes", "Clientes", true, true, true));
        list.add(new RecursoPermissao("financeiro", "Financeiro", true, false, false));
        list.add(new RecursoPermissao("relatorios", "Relatorios", true, false, false));
        return list;
    }

    public static class PermissoesForm {
        private List<RecursoPermissao> recursos;

        public List<RecursoPermissao> getRecursos() {
            return recursos;
        }

        public void setRecursos(List<RecursoPermissao> recursos) {
            this.recursos = recursos;
        }
    }

    public static class RecursoPermissao {
        private String id;
        private String nome;
        private boolean admin;
        private boolean farmaceutico;
        private boolean caixa;

        public RecursoPermissao(String id, String nome, boolean admin, boolean farmaceutico, boolean caixa) {
            this.id = id;
            this.nome = nome;
            this.admin = admin;
            this.farmaceutico = farmaceutico;
            this.caixa = caixa;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public boolean isFarmaceutico() {
            return farmaceutico;
        }

        public void setFarmaceutico(boolean farmaceutico) {
            this.farmaceutico = farmaceutico;
        }

        public boolean isCaixa() {
            return caixa;
        }

        public void setCaixa(boolean caixa) {
            this.caixa = caixa;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RecursoPermissao that = (RecursoPermissao) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
