// src/main/java/br/com/redemaisfarma/application/report/service/ReportService.java
package br.com.redemaisfarma.application.report.service;

import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ItemPedidoJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.report.vm.ClienteRelatorioVM;
import br.com.redemaisfarma.application.report.vm.ProdutoRelatorioVM;
import br.com.redemaisfarma.application.report.vm.VendasRelatorioVM;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Service
public class ReportService {

    private final PedidoRepository pedidoRepository;

    @SuppressWarnings("all") // será usado nos próximos relatórios (produtos/vendas)
    private final ItemPedidoJpaRepository itemPedidoRepository;

    public ReportService(PedidoRepository pedidoRepository,
                         ItemPedidoJpaRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    // ===== Clientes =====
    public List<ClienteRelatorioVM> listarClienteResumo() {
        return pedidoRepository.listarResumoPorCliente().stream()
                .map(r -> new ClienteRelatorioVM(r.getNome(), r.getQtdPedidos(), r.getValorTotal()))
                .toList();
    }

    // ===== Produtos =====
    public List<ProdutoRelatorioVM> listarProdutoResumo() {
        return itemPedidoRepository.listarResumoProdutos().stream()
                .map(r -> new ProdutoRelatorioVM(r.getNome(), r.getQtd(), r.getTotal()))
                .toList();
    }

    public List<ProdutoRelatorioVM> listarProdutoResumo(LocalDateTime de, LocalDateTime ate) {
        return itemPedidoRepository.listarResumoProdutosPorPeriodo(de, ate).stream()
                .map(r -> new ProdutoRelatorioVM(r.getNome(), r.getQtd(), r.getTotal()))
                .toList();
    }

    // ===== Vendas (por dia) =====
    public record VendasResumo(List<VendasRelatorioVM> linhas, long sumQtd, BigDecimal sumTotal) {}

    public VendasResumo listarVendasPorDia(LocalDate de, LocalDate ate) {
        LocalDateTime ini = (de  != null) ? de.atStartOfDay()               : null;
        LocalDateTime fim = (ate != null) ? ate.plusDays(1).atStartOfDay()  : null; // < fim (exclusivo)

        var rows = pedidoRepository.listarResumoVendasPorDia(ini, fim).stream()
                .map(r -> new VendasRelatorioVM(r.getData(), r.getQtd(), r.getTotal()))
                .toList();

        long sumQtd = rows.stream().mapToLong(VendasRelatorioVM::qtd).sum();
        BigDecimal sumTotal = rows.stream()
                .map(VendasRelatorioVM::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new VendasResumo(rows, sumQtd, sumTotal);
    }

    // ===== PDF: Relatório de Clientes =====
    public byte[] gerarRelatorioClientesPdf() {
        // Locale moderno (evita depreciação do new Locale("pt","BR"))
        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        NumberFormat moeda = NumberFormat.getCurrencyInstance(localePtBr);

        var linhas = listarClienteResumo();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 48, 36); // margens: esq, dir, top, bottom
            PdfWriter.getInstance(doc, baos);

            doc.open();

            // Título
            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Paragraph titulo = new Paragraph("Relatório de Clientes", h1);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(12f);
            doc.add(titulo);

            // Subtítulo com data/hora
            Font sub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            String agora = LocalDateTime.now(ZoneId.systemDefault()).toString().replace('T', ' ');
            Paragraph meta = new Paragraph("Gerado em: " + agora, sub);
            meta.setAlignment(Element.ALIGN_RIGHT);
            meta.setSpacingAfter(8f);
            doc.add(meta);

            // Tabela
            PdfPTable table = new PdfPTable(new float[]{5f, 2f, 3f});
            table.setWidthPercentage(100f);

            Font th = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font td = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Cabeçalho
            addHeader(table, "Cliente", th);
            addHeader(table, "Pedidos", th);
            addHeader(table, "Total (R$)", th);

            long totalPedidos = 0L;
            BigDecimal totalValor = BigDecimal.ZERO;

            for (ClienteRelatorioVM c : linhas) {
                totalPedidos += c.qtdPedidos();
                BigDecimal valor = c.valorTotal() != null ? c.valorTotal() : BigDecimal.ZERO;
                totalValor = totalValor.add(valor);

                addCell(table, c.nome(), td, Element.ALIGN_LEFT);
                addCell(table, String.valueOf(c.qtdPedidos()), td, Element.ALIGN_CENTER);
                addCell(table, moeda.format(valor.setScale(2, RoundingMode.HALF_UP)), td, Element.ALIGN_RIGHT);
            }

            // Rodapé (totais)
            Font tf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            PdfPCell totalLabel = new PdfPCell(new Phrase("Totais", tf));
            totalLabel.setColspan(1);
            totalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalLabel.setBackgroundColor(new Color(235, 235, 235));
            totalLabel.setPadding(6f);
            table.addCell(totalLabel);

            PdfPCell totalQtd = new PdfPCell(new Phrase(String.valueOf(totalPedidos), tf));
            totalQtd.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalQtd.setBackgroundColor(new Color(235, 235, 235));
            totalQtd.setPadding(6f);
            table.addCell(totalQtd);

            PdfPCell totalVal = new PdfPCell(new Phrase(moeda.format(totalValor.setScale(2, RoundingMode.HALF_UP)), tf));
            totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalVal.setBackgroundColor(new Color(235, 235, 235));
            totalVal.setPadding(6f);
            table.addCell(totalVal);

            doc.add(table);
            doc.close();

            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Falha ao gerar PDF de clientes", e);
        }
    }

    // ==== helpers ====
    private static void addHeader(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(32, 64, 128)); // azul-escuro legível
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String texto, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(6f);
        table.addCell(cell);
    }
}
