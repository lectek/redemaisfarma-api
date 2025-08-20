package br.com.redemaisfarma.application.core.exception;

/**
 * Exceção lançada quando um pedido não é localizado no banco de dados. Ideal para sinalizar erro 404 em APIs REST ou
 * validar regras de domínio.
 */
public class PedidoNaoEncontradoException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "O pedido solicitado não foi encontrado.";
    private static final String DEFAULT_CODE = "PEDIDO_NAO_ENCONTRADO";

    private final String codigoErro;

    public PedidoNaoEncontradoException() {
        super(DEFAULT_MESSAGE);
        this.codigoErro = DEFAULT_CODE;
    }

    public PedidoNaoEncontradoException(String message) {
        super(message != null ? message : DEFAULT_MESSAGE);
        this.codigoErro = DEFAULT_CODE;
    }

    public PedidoNaoEncontradoException(String message, Throwable cause) {
        super(message != null ? message : DEFAULT_MESSAGE, cause);
        this.codigoErro = DEFAULT_CODE;
    }

    public PedidoNaoEncontradoException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
        this.codigoErro = DEFAULT_CODE;
    }

    // ✅ Getter adicionado
    public String getCodigoErro() {
        return codigoErro;
    }
}
