package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.application.core.produto.ProdutoVitrineService;
import lombok.Generated;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller de Vitrine / Catálogo de Produtos da área do cliente.
 *
 * Pra quem é este controller?
 * --------------------------------
 * - Para o CLIENTE FINAL navegando na parte aberta do site da farmácia.
 * - Usuário que clicou em "Produtos", "Ver todos" ou entrou direto em /produtos.
 *
 * O que ele organiza / resolve?
 * --------------------------------
 * - Atende a rota GET /produtos QUANDO NÃO existe parâmetro de busca (?q=...).
 * - Monta uma “vitrine” de produtos em destaque (lista básica para navegação).
 * - Preenche o Model com os dados necessários para o template de listagem.
 *
 * Por que ele existe separado do HomeController?
 * --------------------------------
 * - O HomeController cuida da HOME ("/", "/cliente", "/cliente/index"), com hero,
 *   categorias, seções “Para você”, “Novidades”, etc.
 * - Este HomePageController é focado apenas em LISTAGEM de PRODUTOS em /produtos
 *   (vitrine geral), deixando a responsabilidade bem separada.
 */
@Controller
public class HomePageController {

    /**
     * Serviço de vitrine de produtos.
     *
     * Para que serve?
     * ----------------
     * - Encapsula as regras de negócio de "quais produtos mostrar na vitrine".
     * - Ex.: buscar apenas produtos ativos, em estoque, ordenados por relevância,
     *   limitar quantidade etc.
     *
     * Por que importante?
     * ----------------
     * - Mantém o controller fino, apenas orquestrando a tela.
     * - Se amanhã mudar a regra de "destaques", muda no service, não no controller.
     */
    private final ProdutoVitrineService vitrine;

    /**
     * GET /produtos (sem parâmetro q)
     *
     * Pra quem é esta função?
     * -------------------------
     * - Cliente navegando na loja, clicando em "Produtos" ou "Ver todos".
     * - Situação em que o usuário NÃO está digitando um termo de busca (sem ?q=).
     *
     * O que ela faz, passo a passo?
     * -------------------------
     * 1) Chama o ProdutoVitrineService para buscar até 12 produtos de destaque.
     * 2) Verifica se existem produtos disponíveis para exibir.
     * 3) Preenche o Model com a lista e flags de controle.
     * 4) Devolve o nome da view (template Thymeleaf) que renderiza a listagem.
     *
     * Por que o params = {"!q"}?
     * -------------------------
     * - Isso garante que ESTE método só responde quando NÃO houver parâmetro q.
     * - Abre espaço para futuramente existir:
     *     GET /produtos?q=paracetamol
     *   em outro método (ex.: controller de busca), sem conflito de rotas.
     */
    @GetMapping(value = {"/produtos"}, params = {"!q"})
    public String listarProdutos(Model model) {

        // 1) Busca até 12 produtos em destaque para compor a vitrine
        List<ProdutoEntity> destaques = vitrine.listarDestaques(12);

        // 2) Flag para saber se há produtos disponíveis
        boolean hasDisponiveis = (destaques != null && !destaques.isEmpty());

        // 3) Atributos usados pelo template da listagem de produtos
        //    - "destaques"  : lista principal de produtos
        //    - "lista"      : alias (compatibilidade com templates antigos ou componentes genéricos)
        //    - "hasDisponiveis"/"temProdutos": ajudam no front a decidir se mostra grid ou mensagem de vazio
        model.addAttribute("destaques", destaques);
        model.addAttribute("lista", destaques);
        model.addAttribute("hasDisponiveis", hasDisponiveis);
        model.addAttribute("temProdutos", hasDisponiveis);

        // Indica para o layout/header qual página está ativa (útil para menu, breadcrumbs, etc.)
        model.addAttribute("page", "produtos");

        // 4) Template que será renderizado
        // IMPORTANTE:
        // - Idealmente, essa view deve ser uma página própria de catálogo, do tipo:
        //     templates/pages/cliente/produtos/lista.html
        // - Se você ainda não criou essa página, TEMPORARIAMENTE pode apontar para "pages/cliente/index",
        //   mas o cenário profissional é ter uma tela dedicada.
        return "pages/cliente/produtos/lista";
        // return "pages/cliente/index"; // <- fallback possível enquanto você não cria a view própria
    }

    /**
     * Construtor com injeção do ProdutoVitrineService.
     *
     * Pra que serve?
     * ----------------
     * - O Spring injeta automaticamente uma implementação de ProdutoVitrineService.
     * - Isso deixa o controller desacoplado da implementação concreta.
     */
    @Generated
    public HomePageController(ProdutoVitrineService vitrine) {
        this.vitrine = vitrine;
    }
}
