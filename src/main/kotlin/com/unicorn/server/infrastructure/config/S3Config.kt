package com.unicorn.server.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

/**
 * S3Config - S3Client 자격증명은 DefaultCredentialsProvider 체인이 환경에 따라 자동으로 고른다.
 * 로컬: ~/.aws/credentials의 AWS_PROFILE, 배포(EC2): 인스턴스에 붙은 IAM Role의 임시 자격증명.
 */
@Configuration
class S3Config(
    @param:Value("\${app.s3.region}") private val region: String,
) {
    @Bean
    fun s3Client(): S3Client =
        S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()
}
