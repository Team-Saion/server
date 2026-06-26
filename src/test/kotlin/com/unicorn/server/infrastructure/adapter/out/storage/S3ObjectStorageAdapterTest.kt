package com.unicorn.server.infrastructure.adapter.out.storage

import com.unicorn.server.common.port.out.storage.ObjectUploadCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.ByteArrayInputStream

@DisplayName("S3ObjectStorageAdapter unit test")
class S3ObjectStorageAdapterTest {
	private val s3Client = mock(S3Client::class.java)
	private val adapter = S3ObjectStorageAdapter(s3Client, "bucket", "ap-northeast-2")

	@Test
	@DisplayName("Input stream is closed after upload")
	fun upload_whenCompleted_closesInputStream() {
		val inputStream = CloseTrackingInputStream("image".toByteArray())
		`when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
			.thenReturn(PutObjectResponse.builder().build())

		adapter.upload(ObjectUploadCommand("images/profile/key.jpg", "image/jpeg", 5, inputStream))

		assertThat(inputStream.closed).isTrue()
	}

	private class CloseTrackingInputStream(bytes: ByteArray) : ByteArrayInputStream(bytes) {
		var closed = false
			private set

		override fun close() {
			closed = true
			super.close()
		}
	}
}
