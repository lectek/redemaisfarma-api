/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.domain.Cliente
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.domain.Cliente;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class ClienteServiceImpl
implements ClienteService {
    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ClienteServiceImpl(ClienteRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Cliente findById(Long id) {
        ClienteEntity entity = (ClienteEntity)this.repository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente n\u00e3o encontrado: id=" + String.valueOf(id)));
        return this.toDomain(entity);
    }

    @Override
    public List<Cliente> list() {
        return this.repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Cliente create(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Payload de cliente \u00e9 obrigat\u00f3rio.");
        }
        String email = this.normalizeEmail(cliente.getEmail());
        String cpf = this.safeTrim(cliente.getCpf());
        if (email != null && this.repository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail j\u00e1 cadastrado.");
        }
        if (cpf != null && this.repository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("CPF j\u00e1 cadastrado.");
        }
        ClienteEntity entity = this.toEntity(cliente);
        entity.setId(null);
        entity.setEmail(email);
        entity.setCpf(cpf);
        if (this.safeHasText(cliente.getSenha())) {
            entity.setSenha(this.passwordEncoder.encode((CharSequence)cliente.getSenha()));
        }
        ClienteEntity salvo = (ClienteEntity)this.repository.save(entity);
        return this.toDomain(salvo);
    }

    @Override
    @Transactional
    public Cliente update(Long id, Cliente cliente) {
        String novaSenhaPura;
        if (id == null) {
            throw new IllegalArgumentException("Id \u00e9 obrigat\u00f3rio para atualiza\u00e7\u00e3o.");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("Payload de cliente \u00e9 obrigat\u00f3rio.");
        }
        ClienteEntity original = (ClienteEntity)this.repository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente n\u00e3o encontrado: id=" + String.valueOf(id)));
        String novoEmail = this.normalizeEmail(cliente.getEmail());
        String novoCpf = this.safeTrim(cliente.getCpf());
        if (novoEmail != null && !novoEmail.equalsIgnoreCase(this.safe(original.getEmail()))) {
            Optional<ClienteEntity> existente = this.repository.findByEmail(novoEmail);
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new IllegalArgumentException("E-mail j\u00e1 cadastrado.");
            }
            original.setEmail(novoEmail);
        }
        if (novoCpf != null && !novoCpf.equals(this.safe(original.getCpf()))) {
            Optional<ClienteEntity> existenteCpf = this.repository.findByCpf(novoCpf);
            if (existenteCpf.isPresent() && !existenteCpf.get().getId().equals(id)) {
                throw new IllegalArgumentException("CPF j\u00e1 cadastrado.");
            }
            original.setCpf(novoCpf);
        }
        if (cliente.getNome() != null) {
            original.setNome(cliente.getNome());
        }
        if (cliente.getTelefone() != null) {
            original.setTelefone(cliente.getTelefone());
        }
        if (cliente.getDataDeNascimento() != null) {
            original.setDataDeNascimento(cliente.getDataDeNascimento());
        }
        original.setAtivo(cliente.isAtivo());
        if (this.safeHasText(cliente.getSenha()) && !this.passwordEncoder.matches((CharSequence)(novaSenhaPura = cliente.getSenha()), this.safe(original.getSenha()))) {
            original.setSenha(this.passwordEncoder.encode((CharSequence)novaSenhaPura));
        }
        ClienteEntity salvo = (ClienteEntity)this.repository.save(original);
        return this.toDomain(salvo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id \u00e9 obrigat\u00f3rio para exclus\u00e3o.");
        }
        if (!this.repository.existsById(id)) {
            throw new NoSuchElementException("Cliente n\u00e3o encontrado: id=" + String.valueOf(id));
        }
        this.repository.deleteById(id);
    }

    private Cliente toDomain(ClienteEntity e) {
        if (e == null) {
            return null;
        }
        Cliente d = new Cliente();
        d.setId(e.getId());
        d.setNome(e.getNome());
        d.setEmail(e.getEmail());
        d.setTelefone(e.getTelefone());
        d.setCpf(e.getCpf());
        d.setSenha(e.getSenha());
        d.setDataDeNascimento(e.getDataDeNascimento());
        d.setAtivo(e.isAtivo());
        return d;
    }

    private ClienteEntity toEntity(Cliente d) {
        if (d == null) {
            return null;
        }
        ClienteEntity e = new ClienteEntity();
        e.setId(d.getId());
        e.setNome(d.getNome());
        e.setEmail(d.getEmail());
        e.setTelefone(d.getTelefone());
        e.setCpf(d.getCpf());
        e.setSenha(d.getSenha());
        e.setDataDeNascimento(d.getDataDeNascimento());
        e.setAtivo(d.isAtivo());
        return e;
    }

    private String normalizeEmail(String email) {
        if (!this.safeHasText(email)) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String safeTrim(String v) {
        return v == null ? null : v.trim();
    }

    private boolean safeHasText(String v) {
        return v != null && !v.trim().isEmpty();
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}

