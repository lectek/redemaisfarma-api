package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AppSettingEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.AppSettingRepository;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/home")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHomePageController {

    private static final String MAIN_PRODUCT_KEY = "HOME.main_product_id";

    private final ProdutoJpaRepository produtoRepository;
    private final AppSettingRepository settingRepository;

    public AdminHomePageController(
            ProdutoJpaRepository produtoRepository,
            AppSettingRepository settingRepository
    ) {
        this.produtoRepository = produtoRepository;
        this.settingRepository = settingRepository;
    }

    @GetMapping("/produto-principal")
    public String form(Model model) {
        List<ProdutoOption> produtos = produtoRepository
                .findAll(PageRequest.of(0, 200, Sort.by("nome").ascending()))
                .getContent()
                .stream()
                .map(ProdutoOption::from)
                .toList();

        String selecionado = settingRepository.findBySettingKey(MAIN_PRODUCT_KEY)
                .map(AppSettingEntity::getSettingValue)
                .orElse("");

        model.addAttribute("produtos", produtos);
        model.addAttribute("selectedId", selecionado);
        return "pages/admin/home-produto-principal";
    }

    @PostMapping("/produto-principal")
    public String salvar(@RequestParam(name = "produtoId", required = false) String produtoId,
                         RedirectAttributes ra) {
        String id = produtoId == null ? "" : produtoId.trim();
        if (id.isBlank()) {
            settingRepository.findBySettingKey(MAIN_PRODUCT_KEY)
                    .ifPresent(settingRepository::delete);
            ra.addFlashAttribute("success", "Produto principal removido.");
            return "redirect:/admin/home/produto-principal";
        }

        try {
            long parsed = Long.parseLong(id);
            if (parsed <= 0L) {
                ra.addFlashAttribute("error", "Informe um produto valido.");
                return "redirect:/admin/home/produto-principal";
            }
        } catch (NumberFormatException ex) {
            ra.addFlashAttribute("error", "Informe um produto valido.");
            return "redirect:/admin/home/produto-principal";
        }

        AppSettingEntity entity = settingRepository.findBySettingKey(MAIN_PRODUCT_KEY)
                .orElseGet(() -> new AppSettingEntity(MAIN_PRODUCT_KEY, id, "Produto principal da home"));
        entity.setSettingValue(id);
        entity.setDescription("Produto principal da home");
        settingRepository.save(entity);

        ra.addFlashAttribute("success", "Produto principal atualizado.");
        return "redirect:/admin/home/produto-principal";
    }

    public record ProdutoOption(
            @NotNull Long id,
            String nome,
            BigDecimal preco,
            boolean disponivel,
            String imagem
    ) {
        static ProdutoOption from(ProdutoEntity p) {
            return new ProdutoOption(
                    p.getId(),
                    p.getNome(),
                    p.getPrecoVenda(),
                    Boolean.TRUE.equals(p.getDisponivel()),
                    p.getImagem()
            );
        }
    }
}
