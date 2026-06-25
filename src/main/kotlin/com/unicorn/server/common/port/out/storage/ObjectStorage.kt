package com.unicorn.server.common.port.out.storage

/**
 * Object storage(S3, R2 등)를 다루는 인터페이스.
 *
 * HTTP 요청에서 받은 MultipartFile은 web adapter/service 계층에서 ObjectUploadCommand로 변환해서 넘긴다.
 * 어떤 Storage를 쓸 지, 스펙( url, bucket 정보 )가 정해지면 ObjectStorage를 구현하는 Adapter를 작성합니다
 *
 * 이 인터페이스는 파일 유형(이미지/HTML 등)을 모른다. 허용 contentType/최대 용량 검증과 objectKey 생성은
 * 호출하는 use-case가 ObjectType으로 미리 끝내고, 이미 검증된 값만 ObjectUploadCommand에 담아 넘긴다.
 *
 */
interface ObjectStorage {
	fun upload(command: ObjectUploadCommand): StoredObject

	fun delete(objectKey: String)

	fun getUrl(objectKey: String): String
}
