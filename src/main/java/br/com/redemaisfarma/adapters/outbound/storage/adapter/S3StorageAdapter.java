package br.com.redemaisfarma.adapters.outbound.storage.adapter;

import br.com.redemaisfarma.adapters.outbound.storage.exception.StorageException;
import br.com.redemaisfarma.adapters.outbound.storage.model.StoredObject;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class S3StorageAdapter {

    private final S3Client s3;
    private final String bucket;
    private final String prefix;

    public S3StorageAdapter(S3Client s3, String bucket, String prefix) {
        this.s3 = Objects.requireNonNull(s3);
        this.bucket = Objects.requireNonNull(bucket);
        this.prefix = (prefix == null || prefix.isBlank()) ? "" : (prefix.endsWith("/") ? prefix : prefix + "/");
    }

    public String upload(InputStream in, long contentLength, String contentType, String key) {
        try {
            String k = prefix + key;

            PutObjectRequest.Builder builder = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(k)
                    .contentLength(contentLength);

            if (contentType != null) {
                builder = builder.contentType(contentType);
            }

            s3.putObject(builder.build(), RequestBody.fromInputStream(in, contentLength));
            return k;
        } catch (S3Exception e) {
            throw new StorageException("Falha ao enviar para S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (SdkException e) {
            throw new StorageException("Falha de comunicação com AWS S3", e);
        } catch (Exception e) {
            throw new StorageException("Falha ao enviar para S3", e);
        }
    }

    public StoredObject download(String key) {
        try {
            String k = prefix + key;
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(k)
                    .build();

            ResponseInputStream<GetObjectResponse> resp = s3.getObject(req);
            GetObjectResponse meta = resp.response();

            String ct = meta.contentType();
            long len = meta.contentLength();
            // O caller deve fechar o InputStream (resp) quando terminar de usar
            return new StoredObject(k, ct, len, resp);
        } catch (NoSuchKeyException e) {
            throw new StorageException("Objeto não encontrado no S3", e);
        } catch (S3Exception e) {
            throw new StorageException("Erro ao baixar do S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (SdkException e) {
            throw new StorageException("Falha de comunicação com AWS S3", e);
        }
    }

    public void delete(String key) {
        try {
            String k = prefix + key;
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(k)
                    .build());
        } catch (S3Exception e) {
            throw new StorageException("Falha ao excluir no S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (SdkException e) {
            throw new StorageException("Falha de comunicação com AWS S3", e);
        }
    }

    public String publicUrl(String key, String region) {
        String k = prefix + key;
        String enc = URLEncoder.encode(k, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, enc);
    }
}
