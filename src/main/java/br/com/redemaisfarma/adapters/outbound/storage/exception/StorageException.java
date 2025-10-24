/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.storage.exception;

public class StorageException
extends RuntimeException {
    public StorageException(String m) {
        super(m);
    }

    public StorageException(String m, Throwable c) {
        super(m, c);
    }
}

