/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  software.amazon.awssdk.core.ResponseInputStream
 *  software.amazon.awssdk.core.exception.SdkException
 *  software.amazon.awssdk.core.sync.RequestBody
 *  software.amazon.awssdk.services.s3.S3Client
 *  software.amazon.awssdk.services.s3.model.DeleteObjectRequest
 *  software.amazon.awssdk.services.s3.model.GetObjectRequest
 *  software.amazon.awssdk.services.s3.model.GetObjectResponse
 *  software.amazon.awssdk.services.s3.model.NoSuchKeyException
 *  software.amazon.awssdk.services.s3.model.PutObjectRequest
 *  software.amazon.awssdk.services.s3.model.PutObjectRequest$Builder
 *  software.amazon.awssdk.services.s3.model.S3Exception
 */
package br.com.redemaisfarma.adapters.outbound.storage.adapter;

import br.com.redemaisfarma.adapters.outbound.storage.exception.StorageException;
import br.com.redemaisfarma.adapters.outbound.storage.model.StoredObject;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3StorageAdapter {
    private final S3Client s3;
    private final String bucket;
    private final String prefix;

    public S3StorageAdapter(S3Client s3, String bucket, String prefix) {
        this.s3 = Objects.requireNonNull(s3);
        this.bucket = Objects.requireNonNull(bucket);
        this.prefix = prefix == null || prefix.isBlank() ? "" : (prefix.endsWith("/") ? prefix : prefix + "/");
    }

    public String upload(InputStream in, long contentLength, String contentType, String key) {
        try {
            String k = this.prefix + key;
            PutObjectRequest.Builder b = PutObjectRequest.builder().bucket(this.bucket).key(k).contentLength(Long.valueOf(contentLength));
            if (contentType != null) {
                b.contentType(contentType);
            }
            this.s3.putObject((PutObjectRequest)b.build(), RequestBody.fromInputStream((InputStream)in, (long)contentLength));
            return k;
        }
        catch (S3Exception e) {
            throw new StorageException("Falha ao enviar para S3: " + e.awsErrorDetails().errorMessage(), e);
        }
        catch (SdkException e) {
            throw new StorageException("Falha de comunica\u00e7\u00e3o com AWS S3", e);
        }
        catch (Exception e) {
            throw new StorageException("Falha ao enviar para S3", e);
        }
    }

    public StoredObject download(String key) {
        try {
            String k = this.prefix + key;
            GetObjectRequest req = (GetObjectRequest)GetObjectRequest.builder().bucket(this.bucket).key(k).build();
            ResponseInputStream resp = this.s3.getObject(req);
            String ct = ((GetObjectResponse)resp.response()).contentType();
            long len = ((GetObjectResponse)resp.response()).contentLength();
            return new StoredObject(k, ct, len, (InputStream)resp);
        }
        catch (NoSuchKeyException e) {
            throw new StorageException("Objeto n\u00e3o encontrado no S3", e);
        }
        catch (S3Exception e) {
            throw new StorageException("Erro ao baixar do S3: " + e.awsErrorDetails().errorMessage(), e);
        }
        catch (SdkException e) {
            throw new StorageException("Falha de comunica\u00e7\u00e3o com AWS S3", e);
        }
    }

    public void delete(String key) {
        try {
            String k = this.prefix + key;
            this.s3.deleteObject((DeleteObjectRequest)DeleteObjectRequest.builder().bucket(this.bucket).key(k).build());
        }
        catch (S3Exception e) {
            throw new StorageException("Falha ao excluir no S3: " + e.awsErrorDetails().errorMessage(), e);
        }
        catch (SdkException e) {
            throw new StorageException("Falha de comunica\u00e7\u00e3o com AWS S3", e);
        }
    }

    public String publicUrl(String key, String region) {
        String k = this.prefix + key;
        String enc = URLEncoder.encode(k, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(this.bucket, region, enc);
    }
}

