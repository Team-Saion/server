package com.unicorn.server.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

/**
 * S3Config - endpoint 가 비어있으면 운영 모드(DefaultCredentialsProvider),
 * endpoint 값이 있으면 로컬 MinIO 모드(StaticCredentialsProvider + endpointOverride)로 동작한다.
 * path-style 은 MinIO 호환을 위해 항상 활성화한다.
 */
@Configuration
class S3Config(
    @param:Value("\${app.s3.region}") private val region: String,
    @param:Value("\${app.s3.endpoint:}") private val endpoint: String,
    @param:Value("\${app.s3.access-key:}") private val accessKey: String,
    @param:Value("\${app.s3.secret-key:}") private val secretKey: String,
) {
    @Bean
    fun s3Client(): S3Client {
        val builder = S3Client.builder()
            .region(Region.of(region))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build(),
            )

        if (endpoint.isNotBlank()) {
            builder
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey),
                    ),
                )
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build())
        }

        return builder.build()
    }
}
