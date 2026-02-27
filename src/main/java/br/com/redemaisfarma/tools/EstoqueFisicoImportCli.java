package br.com.redemaisfarma.tools;

import br.com.redemaisfarma.application.service.EstoqueFisicoImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "estoque-fisico.import.run", havingValue = "true", matchIfMissing = false)
public class EstoqueFisicoImportCli implements ApplicationRunner {

    private final EstoqueFisicoImportService estoqueFisicoImportService;

    @Value("${estoque-fisico.import.exit:true}")
    private boolean exitAfterRun;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("[estoque-csv-import] iniciando importacao completa para o banco...");
        EstoqueFisicoImportService.ImportacaoResumo resumo = this.estoqueFisicoImportService.importarTodosComoNaoDisponiveis();
        System.out.printf(
                "[estoque-csv-import] finalizado | lidos=%d inseridos=%d atualizados=%d ignorados=%d erros=%d%n",
                resumo.lidos(),
                resumo.inseridos(),
                resumo.atualizados(),
                resumo.ignorados(),
                resumo.erros()
        );

        if (this.exitAfterRun) {
            System.exit(0);
        }
    }
}
