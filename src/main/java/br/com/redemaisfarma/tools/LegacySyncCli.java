// src/main/java/br/com/redemaisfarma/tools/LegacySyncCli.java
package br.com.redemaisfarma.tools;

import br.com.redemaisfarma.application.service.SincronizacaoCatalogoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"legacy"}) // só carrega com o perfil legacy ativo
public class LegacySyncCli implements ApplicationRunner {

    private final SincronizacaoCatalogoService sync;

    @Value("${legacy.sync.run:false}")
    private boolean run;

    public LegacySyncCli(SincronizacaoCatalogoService sync) {
        this.sync = sync;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!run) return; // só executa quando passar --legacy.sync.run=true
        System.out.println("[legacy-sync] Iniciando sincronização Firebird -> MySQL (estoque=0) ...");
        var resumo = sync.sincronizarTudo();
        System.out.printf("[legacy-sync] OK | lidos=%d inseridos=%d atualizados=%d ignorados=%d erros=%d%n",
                resumo.lidos(), resumo.inseridos(), resumo.atualizados(), resumo.ignorados(), resumo.erros());
        System.exit(0);
    }
}
