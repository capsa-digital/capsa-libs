package digital.capsa.it.json

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath

object JsonPathModifier {

    fun modifyJson(json: String, transformationData: Map<String, Any?>): String {
        val document = JsonPath.using(Configuration.defaultConfiguration()).parse(json)

        for ((path, value) in transformationData) {
            document.set(path, value)
        }
        return document.jsonString()
    }
}