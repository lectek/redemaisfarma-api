package br.com.redemaisfarma.adapters.outbound.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /** provider: local | s3 */
    @NotBlank
    private String provider = "local";

    // Local
    private String localBasePath = "storage"; // relativo ao working dir

    // S3
    private String s3Bucket;
    private String s3Region;
    /** prefixo/pasta dentro do bucket, opcional */
    private String s3Prefix;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getLocalBasePath() {
        return localBasePath;
    }

    public void setLocalBasePath(String localBasePath) {
        this.localBasePath = localBasePath;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3Region() {
        return s3Region;
    }

    public void setS3Region(String s3Region) {
        this.s3Region = s3Region;
    }

    public String getS3Prefix() {
        return s3Prefix;
    }

    public void setS3Prefix(String s3Prefix) {
        this.s3Prefix = s3Prefix;
    }
}
