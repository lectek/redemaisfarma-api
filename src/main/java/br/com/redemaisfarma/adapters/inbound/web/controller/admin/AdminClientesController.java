package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/clientes")
public class AdminClientesController {

    /**
     * First page index.
     */
    private static final int PAGE_MIN = 0;

    /**
     * Minimum allowed page size.
     */
    private static final int SIZE_MIN = 10;

    /**
     * Maximum allowed page size.
     */
    private static final int SIZE_MAX = 200;

    /**
     * Default page size request parameter.
     */
    private static final String SIZE_DEFAULT_PARAM = "50";

    /**
     * Repository for customer data.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Creates controller with repository dependency.
     *
     * @param repository customer repository
     */
    public AdminClientesController(final ClienteRepository repository) {
        this.clienteRepository = repository;
    }

    /**
     * Lists customers for admin screen with filters and pagination.
     *
     * @param q optional search term
     * @param status optional status filter
     * @param page page index
     * @param size page size
     * @param model view model
     * @return customers list page
     */
    @GetMapping({"", "/", "/lista"})
    public String lista(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "status", required = false)
            final String status,
            @RequestParam(value = "page", defaultValue = "0") final int page,
            @RequestParam(value = "size", defaultValue = SIZE_DEFAULT_PARAM)
            final int size,
            final Model model
    ) {
        final String termo = normalize(q);
        final String statusSelecionado = normalizeStatus(status);
        final Boolean ativo = toAtivo(statusSelecionado).orElse(null);
        final int safePage = Math.max(PAGE_MIN, page);
        final int safeSize = Math.clamp(size, SIZE_MIN, SIZE_MAX);

        final Page<ClienteEntity> pageData = clienteRepository.searchAdmin(
                termo,
                ativo,
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "id")
                )
        );

        final long total = clienteRepository.count();
        final long ativos = clienteRepository.countByAtivoTrue();
        final long inativos = Math.max(PAGE_MIN, total - ativos);

        model.addAttribute("clientes", pageData.getContent());
        model.addAttribute("totalClientes", total);
        model.addAttribute("totalClientesAtivos", ativos);
        model.addAttribute("totalClientesInativos", inativos);
        model.addAttribute("statusSelecionado", statusSelecionado);
        model.addAttribute("totalFiltrados", pageData.getTotalElements());

        return "pages/admin/clientes/lista";
    }

    /**
     * Normalizes free-text values.
     *
     * @param value source value
     * @return normalized value or null
     */
    private static String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Normalizes status to upper-case enum-like value.
     *
     * @param value raw status
     * @return normalized status
     */
    private static String normalizeStatus(final String value) {
        final String normalized = normalize(value);
        if (normalized == null) {
            return "";
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    /**
     * Maps status filter to active flag.
     *
     * @param status normalized status
     * @return active flag when status is known
     */
    private static Optional<Boolean> toAtivo(final String status) {
        if ("ATIVO".equals(status)) {
            return Optional.of(Boolean.TRUE);
        }
        if ("INATIVO".equals(status)) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }
}
