package br.com.redemaisfarma.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CadastroProdutoRequestDTO {

    @NotBlank
    private String nome;

    @Positive
    private double preco;

    // Getters e Setters
}

