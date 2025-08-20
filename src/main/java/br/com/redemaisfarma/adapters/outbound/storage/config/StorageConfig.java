package br.com.redemaisfarma.adapters.outbound.storage.config;

import br.com.redemaisfarma.adapters.outbound.storage.adapter.LocalStorageAdapter;
import br.com.redemaisfarma.adapters.outbound.storage.adapter.S3StorageAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
    public LocalStorageAdapter localStorageAdapter(StorageProperties props) {
        return new LocalStorageAdapter(props.getLocalBasePath());
    }

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    public S3StorageAdapter s3StorageAdapter(StorageProperties props) {
        S3Client s3 = S3Client.builder().region(Region.of(props.getS3Region()))
                .credentialsProvider(DefaultCredentialsProvider.create()).build();
        return new S3StorageAdapter(s3, props.getS3Bucket(), props.getS3Prefix());
    }
}
