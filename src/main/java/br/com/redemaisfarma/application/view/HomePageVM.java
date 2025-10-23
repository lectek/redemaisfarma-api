// src/main/java/br/com/redemaisfarma/application/view/HomePageVM.java
package br.com.redemaisfarma.application.view;

import java.util.List;

public record HomePageVM(
        List<ProductCardVM> paraVoce,
        List<ProductCardVM> maisVendidos,
        List<ProductCardVM> novidades,
        List<ProductCardVM> destaque
) {}
