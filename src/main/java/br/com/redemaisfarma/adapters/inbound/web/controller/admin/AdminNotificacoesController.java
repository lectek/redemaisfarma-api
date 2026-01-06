package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notificacoes")
@Validated
public class AdminNotificacoesController {

    private final ClienteNotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    public AdminNotificacoesController(ClienteNotificacaoRepository notificacaoRepository,
                                       UsuarioRepository usuarioRepository,
                                       ProdutoRepository produtoRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    public String page() {
        return "pages/admin/notificacoes";
    }

    @PostMapping("/api/enviar")
    public ResponseEntity<?> enviar(@Valid @RequestBody AdminNotificacaoRequest req) {
        List<ClienteNotificacaoEntity> criadas = new ArrayList<>();
        for (Long userId : req.usuarioIds()) {
            Optional<UsuarioEntity> usuario = usuarioRepository.findById(userId);
            if (usuario.isEmpty()) continue;
            ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
            notificacao.setUsuario(usuario.get());
            notificacao.setTipo(req.tipo());
            notificacao.setTitulo(req.titulo());
            notificacao.setMensagem(req.mensagem());
            criadas.add(notificacao);
        }
        if (!criadas.isEmpty()) {
            notificacaoRepository.saveAll(criadas);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/enviar/todos")
    public ResponseEntity<?> enviarTodos(@Valid @RequestBody AdminNotificacaoBroadcastRequest req) {
        List<UsuarioEntity> usuarios = usuarioRepository.findAll();
        List<ClienteNotificacaoEntity> criadas = new ArrayList<>();
        for (UsuarioEntity usuario : usuarios) {
            ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
            notificacao.setUsuario(usuario);
            notificacao.setTipo(req.tipo());
            notificacao.setTitulo(req.titulo());
            notificacao.setMensagem(req.mensagem());
            criadas.add(notificacao);
        }
        if (!criadas.isEmpty()) {
            notificacaoRepository.saveAll(criadas);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/enviar/usuario/{id}")
    public ResponseEntity<?> enviarParaUsuario(@PathVariable("id") Long id,
                                               @Valid @RequestBody AdminNotificacaoSingleRequest req) {
        Optional<UsuarioEntity> usuario = usuarioRepository.findById(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
        notificacao.setUsuario(usuario.get());
        notificacao.setTipo(req.tipo());
        notificacao.setTitulo(req.titulo());
        notificacao.setMensagem(req.mensagem());
        notificacaoRepository.save(notificacao);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/enviar/estoque-baixo")
    public ResponseEntity<?> enviarEstoqueBaixo() {
        int limite = 10;
        int qtdBaixo = produtoRepository.findComEstoqueBaixo(limite).size();
        if (qtdBaixo <= 0) {
            return ResponseEntity.noContent().build();
        }
        List<UsuarioEntity> usuarios = usuarioRepository.findAll();
        List<ClienteNotificacaoEntity> criadas = new ArrayList<>();
        String titulo = "Estoque baixo";
        String mensagem = qtdBaixo + " produtos com estoque em " + limite + " unidades ou menos.";
        for (UsuarioEntity usuario : usuarios) {
            ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
            notificacao.setUsuario(usuario);
            notificacao.setTipo("ESTOQUE");
            notificacao.setTitulo(titulo);
            notificacao.setMensagem(mensagem);
            criadas.add(notificacao);
        }
        if (!criadas.isEmpty()) {
            notificacaoRepository.saveAll(criadas);
        }
        return ResponseEntity.ok().build();
    }

    public record AdminNotificacaoRequest(
            @NotEmpty List<Long> usuarioIds,
            @NotBlank @Size(max = 40) String tipo,
            @NotBlank @Size(max = 120) String titulo,
            @NotBlank @Size(max = 500) String mensagem
    ) {}

    public record AdminNotificacaoBroadcastRequest(
            @NotBlank @Size(max = 40) String tipo,
            @NotBlank @Size(max = 120) String titulo,
            @NotBlank @Size(max = 500) String mensagem
    ) {}

    public record AdminNotificacaoSingleRequest(
            @NotBlank @Size(max = 40) String tipo,
            @NotBlank @Size(max = 120) String titulo,
            @NotBlank @Size(max = 500) String mensagem
    ) {}
}
