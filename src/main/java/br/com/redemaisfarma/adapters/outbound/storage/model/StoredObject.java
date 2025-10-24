/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.storage.model;

import java.io.InputStream;

public class StoredObject {
    private String key;
    private String contentType;
    private long contentLength;
    private InputStream stream;

    public StoredObject(String key, String contentType, long contentLength, InputStream stream) {
        this.key = key;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.stream = stream;
    }

    public String getKey() {
        return this.key;
    }

    public String getContentType() {
        return this.contentType;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public InputStream getStream() {
        return this.stream;
    }
}

