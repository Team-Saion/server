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
import software.amazon.awssdk.services.s3.S3Utilities
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.ByteArrayInputStream
import java.net.URI

@DisplayName("S3ObjectStorageAdapter unit test")
class S3ObjectStorageAdapterTest {
	private val s3Client = mock(S3Client::class.java)
	private val s3Utilities = mock(S3Utilities::class.java)
	private val adapter = S3ObjectStorageAdapter(s3Client, "bucket")

	@Test
	@DisplayName("Input stream is closed after upload")
	fun upload_whenCompleted_closesInputStream() {
		val inputStream = CloseTrackingInputStream("image".toByteArray())
		`when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
			.thenReturn(PutObjectResponse.builder().build())
		`when`(s3Client.utilities()).thenReturn(s3Utilities)
		`when`(s3Utilities.getUrl(any(GetUrlRequest::class.java)))
			.thenReturn(URI("https://example.com/bucket/images/profile/key.jpg").toURL())

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
