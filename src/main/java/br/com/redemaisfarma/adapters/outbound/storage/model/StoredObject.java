package br.com.redemaisfarma.adapters.outbound.storage.model;

import java.io.InputStream;

public class StoredObject {
    private String key;
    private String contentType;
    private long contentLength;
    private InputStream stream; // lembre-se de fechar após uso

    public StoredObject(String key, String contentType, long contentLength, InputStream stream) {
        this.key = key;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.stream = stream;
    }

    public String getKey() {
        return key;
    }

    public String getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public InputStream getStream() {
        return stream;
    }
}
