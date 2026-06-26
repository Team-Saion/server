package com.unicorn.server.infrastructure.adapter.out.storage

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.storage.ObjectStorage
import com.unicorn.server.common.port.out.storage.ObjectUploadCommand
import com.unicorn.server.common.port.out.storage.StoredObject
import com.unicorn.server.common.port.out.storage.exception.ObjectStorageErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

/**
 * S3ObjectStorageAdapter - EC2 IAM Role 기반 임시 자격증명으로 S3에 접근하는 ObjectStorage 구현체.
 * 호출자가 ObjectType으로 이미 검증/objectKey 생성을 끝낸 ObjectUploadCommand만 받는다고 가정한다.
 */
@Component
class S3ObjectStorageAdapter(
    private val s3Client: S3Client,
    @param:Value("\${app.s3.bucket}") private val bucket: String,
    @param:Value("\${app.s3.region}") private val region: String,
) : ObjectStorage {
    override fun upload(command: ObjectUploadCommand): StoredObject {
        if (command.objectKey.isBlank()) {
            throw BusinessException(ObjectStorageErrorCode.INVALID_OBJECT_KEY, command.objectKey)
        }

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(command.objectKey)
            .contentType(command.contentType)
            .contentLength(command.contentLength)
            .build()

        try {
            command.inputStream.use { inputStream ->
                s3Client.putObject(request, RequestBody.fromInputStream(inputStream, command.contentLength))
            }
        } catch (e: SdkException) {
            throw BusinessException(ObjectStorageErrorCode.UPLOAD_FAILED, command.objectKey, e)
        }

        return StoredObject(command.objectKey, getUrl(command.objectKey), command.contentType, command.contentLength)
    }

    override fun delete(objectKey: String) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build())
        } catch (e: SdkException) {
            throw BusinessException(ObjectStorageErrorCode.DELETE_FAILED, objectKey, e)
        }
    }

    override fun getUrl(objectKey: String): String =
        "https://$bucket.s3.$region.amazonaws.com/$objectKey"
}
