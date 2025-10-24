/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.http.cliente.exception;

import br.com.redemaisfarma.adapters.outbound.http.cliente.exception.ClienteClientException;

public class ClienteTimeoutException
extends ClienteClientException {
    public ClienteTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

