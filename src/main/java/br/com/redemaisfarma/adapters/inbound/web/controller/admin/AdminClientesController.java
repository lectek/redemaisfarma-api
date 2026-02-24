package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
@RequestMapping("/admin/clientes")
public class AdminClientesController {

    private final ClienteRepository clienteRepository;

    public AdminClientesController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping({"", "/", "/lista"})
    public String lista(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            Model model
    ) {
        String termo = normalize(q);
        String statusSelecionado = normalizeStatus(status);
        Boolean ativo = toAtivo(statusSelecionado);
        int safePage = Math.max(0, page);
        int safeSize = Math.max(10, Math.min(size, 200));

        Page<ClienteEntity> pageData = clienteRepository.searchAdmin(
                termo,
                ativo,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"))
        );

        long total = clienteRepository.count();
        long ativos = clienteRepository.countByAtivoTrue();
        long inativos = Math.max(0, total - ativos);

        model.addAttribute("clientes", pageData.getContent());
        model.addAttribute("totalClientes", total);
        model.addAttribute("totalClientesAtivos", ativos);
        model.addAttribute("totalClientesInativos", inativos);
        model.addAttribute("statusSelecionado", statusSelecionado);
        model.addAttribute("totalFiltrados", pageData.getTotalElements());

        return "pages/admin/clientes/lista";
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeStatus(String value) {
        String normalized = normalize(value);
        if (normalized == null) return "";
        return normalized.toUpperCase(Locale.ROOT);
    }

    private static Boolean toAtivo(String status) {
        if ("ATIVO".equals(status)) return Boolean.TRUE;
        if ("INATIVO".equals(status)) return Boolean.FALSE;
        return null;
    }
}
