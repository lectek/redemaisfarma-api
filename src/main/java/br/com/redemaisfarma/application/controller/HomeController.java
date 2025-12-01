package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.view.HomePageVM;
import br.com.redemaisfarma.domain.catalogo.HomepageCatalogFacade;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    // 🔎 Logger para registrar no log o que está acontecendo nessa classe
    @Generated
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    /**
     * Facade de catálogo da home.
     *
     * Para QUEM isso existe?
     * - Para a camada web (controllers) conseguir pedir, em UMA chamada,
     *   todo o "pacote" de informações da vitrine da home (destaques,
     *   novidades, mais vendidos, para você).
     *
     * Por QUE existe?
     * - Para esconder a complexidade de várias consultas, serviços e adaptações
     *   em uma fachada única: HomepageCatalogFacade.
     *
     * Quem usa?
     * - Esse HomeController.
     * - Indiretamente, os templates da home, que consomem o HomePageVM que vem da facade.
     */
    private final HomepageCatalogFacade facade;

    /**
     * Construtor com injeção de dependência da facade.
     *
     * Quem chama?
     * - O próprio Spring, quando sobe o contexto da aplicação.
     *
     * Para QUE serve?
     * - Para garantir que, ao criar o HomeController, ele já tenha em mãos
     *   um objeto HomepageCatalogFacade pronto para ser usado nas rotas.
     */
    @Generated
    public HomeController(HomepageCatalogFacade facade) {
        this.facade = facade;
    }

    /**
     * 🚪 PÁGINA INICIAL PÚBLICA DO CLIENTE (VITRINE)
     *
     * Rotas atendidas:
     *  - GET "/"
     *  - GET "/cliente"
     *  - GET "/cliente/index"
     *
     * PARA QUEM é essa função?
     * - Para o CLIENTE FINAL da farmácia usando o navegador:
     *   - quando ele entra no domínio principal ("/"),
     *   - quando acessa "/cliente",
     *   - ou "/cliente/index".
     *
     * Em QUAL momento do fluxo?
     * - É o ponto de entrada da "loja online".
     * - Aqui a pessoa vê os destaques, novidades, mais vendidos, etc.
     *
     * O QUE ela faz, tecnicamente?
     * - Pede para a HomepageCatalogFacade montar um HomePageVM
     *   contendo listas de produtos organizadas em blocos:
     *   → vm.destaque
     *   → vm.paraVoce
     *   → vm.novidades
     *   → vm.maisVendidos
     * - Decide se vai mostrar mensagem de boas-vindas (showWelcome)
     *   com base no parâmetro "onboarding".
     * - Prepara o Model para o template Thymeleaf "pages/cliente/index".
     *
     * Como é usada no HTML?
     * - O template `templates/pages/cliente/index.html` lê:
     *   - `${vm}` → para montar os cards
     *   - `${showWelcome}` → para decidir se mostra a mensagem de onboarding
     *   - `${page}` → para o layout saber que está na home do cliente.
     */
    @GetMapping({"/", "/cliente", "/cliente/index"})
    public String exibirPaginaInicial(
            @RequestParam(value = "onboarding", required = false) String onboarding,
            Model model) {

        HomePageVM vm = null;

        try {
            // Pede para a fachada montar o "pacote" da home:
            // esse buildHomepage provavelmente faz consultas em banco,
            // regras de negócio, conversão para ProductCardVM etc.
            vm = facade.buildHomepage();
            log.debug("HomePageVM carregada? {}", (vm != null ? 1 : 0));
        } catch (Exception e) {
            // Se der erro, loga mas não derruba a aplicação.
            // A tela pode abrir "mais vazia", sem alguns blocos.
            log.error("Falha ao montar HomePageVM", e);
        }

        // ViewModel com toda a vitrine da home
        model.addAttribute("vm", vm);

        // Flag usada no template para saber se mostra o módulo de boas-vindas
        model.addAttribute("showWelcome", isOnboarding(onboarding));

        // Usado pelo layout (ex.: para marcar o menu "Home" ativo)
        model.addAttribute("page", "home");

        // Diz para o Spring qual template Thymeleaf será renderizado
        // → src/main/resources/templates/pages/cliente/index.html
        return "pages/cliente/index";
    }

    /**
     * 💳 PÁGINA DE CHECKOUT (RESUMO E FINALIZAÇÃO DA COMPRA)
     *
     * Rota:
     *  - GET "/checkout"
     *
     * PARA QUEM é essa função?
     * - Para o cliente logado que já:
     *   1) Navegou na vitrine
     *   2) Adicionou itens no carrinho
     *   3) Está no fluxo de finalizar a compra
     *
     * Em QUAL momento do fluxo?
     * - Depois que o carrinho já está montado.
     * - Antes do pagamento de fato, dependendo de como você implementar.
     *
     * O QUE ela faz?
     * - Prepara o atributo "page" para o layout.
     * - Encaminha para o template de checkout.
     *
     * Segurança:
     * - Quem decide se precisa estar logado ou não é o SecurityConfig.
     *   Normalmente, checkout deve ser protegido.
     *
     * Template renderizado:
     * - "pages/cliente/checkout/checkout"
     *   → src/main/resources/templates/pages/cliente/checkout/checkout.html
     */
    @GetMapping("/checkout")
    public String exibirCheckout(Model model) {
        model.addAttribute("page", "checkout");
        return "pages/cliente/checkout/checkout";
    }

    /**
     * 🛒 PÁGINA DO CARRINHO
     *
     * Rota:
     *  - GET "/carrinho"
     *
     * PARA QUEM é essa função?
     * - Para o cliente que já começou a montar compras:
     *   - Ele clicou em "Adicionar ao carrinho" em algum produto
     *   - Ou clicou no ícone do carrinho na barra superior da loja.
     *
     * Em QUAL momento do fluxo?
     * - Entre navegar na vitrine e ir para o checkout.
     * - O cliente revisa itens, quantidades, preços e frete.
     *
     * O QUE ela faz?
     * - Diz para o layout que a página atual é "carrinho"
     * - Redireciona para o template de carrinho.
     *
     * Onde o conteúdo real do carrinho é montado?
     * - Normalmente:
     *   - via sessão do usuário
     *   - ou via API REST que retorna os itens do carrinho
     *   - e o template Thymeleaf consome isso (isso você ainda pode ligar depois).
     *
     * Template renderizado:
     * - "pages/cliente/carrinho/carrinho"
     *   → src/main/resources/templates/pages/cliente/carrinho/carrinho.html
     */
    @GetMapping("/carrinho")
    public String exibirCarrinho(Model model) {
        model.addAttribute("page", "carrinho");
        return "pages/cliente/carrinho/carrinho";
    }

    /**
     * 🏪 PÁGINA INSTITUCIONAL "SOBRE"
     *
     * Rota:
     *  - GET "/sobre"
     *
     * PARA QUEM é essa função?
     * - Para qualquer pessoa (cliente, parceiro, curioso) que quer saber:
     *   - Quem é a RedeMaisFarma
     *   - Onde fica
     *   - Horários de funcionamento
     *   - Missão, visão, valores, contatos, etc.
     *
     * Em QUAL momento do fluxo?
     * - Não está diretamente na jornada de compra.
     * - É mais parte da "credibilidade" e transparência da farmácia.
     *
     * O QUE ela faz?
     * - Seta "page = sobre" para o layout.
     * - Encaminha para o template estático "sobre".
     *
     * Template:
     * - "pages/cliente/sobre"
     *   → src/main/resources/templates/pages/cliente/sobre.html
     */
    @GetMapping("/sobre")
    public String exibirSobre(Model model) {
        model.addAttribute("page", "sobre");
        return "pages/cliente/sobre";
    }

    /**
     * ⚙️ Função utilitária para interpretar o parâmetro "onboarding"
     *
     * PARA QUEM é essa função?
     * - Para o próprio HomeController, como uma ajudante privada.
     * - Não é exposta como rota HTTP.
     *
     * Em QUAL momento é usada?
     * - Dentro de exibirPaginaInicial(), toda vez que alguém acessa
     *   "/", "/cliente" ou "/cliente/index" com (ou sem) o parâmetro "onboarding".
     *
     * O QUE ela faz?
     * - Converte strings de URL em um booleano.
     * - Retorna true se o valor indicar "primeira vez / onboarding".
     *
     * Exemplos que retornam true:
     *  - /?onboarding=1
     *  - /?onboarding=true
     *  - /?onboarding=yes
     *
     * Exemplos que retornam false:
     *  - sem parâmetro "onboarding"
     *  - /?onboarding=0
     *  - /?onboarding=abc
     */
    private boolean isOnboarding(String v) {
        if (v == null) return false;
        return "1".equals(v)
                || "true".equalsIgnoreCase(v)
                || "yes".equalsIgnoreCase(v);
    }
}
