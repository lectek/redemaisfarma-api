package br.com.redemaisfarma.application.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import br.com.redemaisfarma.application.dto.request.CadastroClienteRequestDTO;

/**
 * Controller responsÃƒÂ¡vel por lidar com o cadastro de novos clientes no sistema Embalando. Faz parte da camada web da
 * arquitetura hexagonal e se comunica com a camada de apresentaÃƒÂ§ÃƒÂ£o (HTML/Thymeleaf).
 */
@Controller
@RequestMapping("/cadastro-cliente")
public class ClienteController {

    /**
     * Exibe a pÃƒÂ¡gina de formulÃƒÂ¡rio de cadastro de cliente. MÃƒÂ©todo acessado via GET:
     * http://localhost:8080/cadastro-cliente
     *
     * @param model
     *            Modelo usado para preencher os dados no formulÃƒÂ¡rio
     *
     * @return caminho da view HTML
     */
    @GetMapping
    public String mostrarFormularioCadastro(Model model) {
        // Instancia um formulÃƒÂ¡rio vazio para preencher o th:object no HTML
        model.addAttribute("cadastroForm", new CadastroClienteRequestDTO());
        return "pages/cadastro-cliente"; // HTML localizado em templates/pages/
    }

    /**
     * Processa os dados enviados no formulÃƒÂ¡rio de cadastro de cliente. Valida os campos usando Bean Validation (@Valid)
     * e retorna erros, se houver.
     *
     * @param form
     *            objeto que contÃƒÂ©m os dados do formulÃƒÂ¡rio
     * @param result
     *            resultado da validaÃƒÂ§ÃƒÂ£o automÃƒÂ¡tica
     *
     * @return redireciona ou retorna ÃƒÂ  pÃƒÂ¡gina de cadastro com erros
     */
    @PostMapping
    public String processarCadastro(@Valid @ModelAttribute("cadastroForm") CadastroClienteRequestDTO form,
            BindingResult result, Model model) {

        // Verifica se houve erros de validaÃƒÂ§ÃƒÂ£o
        if (result.hasErrors()) {
            // MantÃƒÂ©m o objeto no modelo para mostrar mensagens de erro no HTML
            model.addAttribute("cadastroForm", form);
            return "pages/cadastro-cliente";
        }

        // [Ã°Å¸â€â€™ TO-DO] Implementar lÃƒÂ³gica de verificaÃƒÂ§ÃƒÂ£o de e-mail ÃƒÂºnico
        // [Ã°Å¸â€™Â¾ TO-DO] Salvar dados no banco de dados com serviÃƒÂ§o de persistÃƒÂªncia
        // [Ã°Å¸Â§Â  TO-DO] Iniciar sessÃƒÂ£o do cliente e redirecionar para o painel

        // Por enquanto, redireciona para a tela de login apÃƒÂ³s cadastro
        return "redirect:/login";
    }

    /**
     * [OPCIONAL] Exibe um resumo explicativo ou passo a passo do processo de cadastro Pode ser ativado futuramente como
     * rota de ajuda ao usuÃƒÂ¡rio.
     */
    @GetMapping("/ajuda")
    public String mostrarAjudaCadastro() {
        return "pages/ajuda-cadastro"; // Essa view pode ser criada depois
    }

    /**
     * [FUTURO] MÃƒÂ©todo para exibir uma prÃƒÂ©via de dados do cliente antes de salvar. Pode ser usado para confirmaÃƒÂ§ÃƒÂ£o
     * visual.
     */
    @PostMapping("/pre-visualizar")
    public String preVisualizarCadastro(@Valid @ModelAttribute("cadastroForm") CadastroClienteRequestDTO form,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "pages/cadastro-cliente";
        }

        model.addAttribute("preview", form);
        return "pages/preview-cadastro"; // HTML opcional com resumo dos dados
    }
}
