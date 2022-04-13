package digital.capsa.it.runner


import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@Component
class HttpMultipartFileEntity(
    entityName: String,
    fileName: String,
    fileContent: ByteArray
) : HttpEntity<Any>() {
    var httpEntity: HttpEntity<Any>

    init {
        val contentDisposition: ContentDisposition = ContentDisposition
            .builder(MediaType.MULTIPART_FORM_DATA.subtype)
            .name(entityName)
            .filename(fileName)
            .build()

        val fileMap: MultiValueMap<String, String> = LinkedMultiValueMap()
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        httpEntity = HttpEntity<Any>(fileContent, fileMap)
    }
}
