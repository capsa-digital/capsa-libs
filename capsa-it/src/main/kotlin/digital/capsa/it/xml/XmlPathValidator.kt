package digital.capsa.it.xml

import assertk.assertThat
import assertk.assertions.matches
import digital.capsa.it.validation.OpType
import digital.capsa.it.validation.ValidationRule
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.test.assertEquals
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.w3c.dom.NodeList
import org.xml.sax.InputSource


object XmlPathValidator {

    init {
        setIdeaIoUseFallback()
    }

    fun assertXml(xml: String, rules: List<ValidationRule>) {
        val factory = DocumentBuilderFactory.newInstance()
        val builder: DocumentBuilder = factory.newDocumentBuilder()
        val document = builder.parse(InputSource(StringReader(xml)))
        for (rule in rules) {
            val valueList: List<Any?> = if (rule.value is List<*>) {
                rule.value
            } else {
                listOf(rule.value)
            }
            var nodes: NodeList?
            try {
                val xpath: XPath = XPathFactory.newInstance().newXPath()
                nodes = xpath.evaluate(
                    rule.path, document,
                    XPathConstants.NODESET
                ) as NodeList
            } catch (e: Exception) {
                throw Error("Path not found, document: $document, path: ${rule.path}", e)
            }
            if (nodes.length <= 0) {
                throw Error("Path not found, document: $document, path: ${rule.path}")
            } else if (nodes.length > 1) {
                if (rule.op != OpType.equal) {
                    throw Error("'${rule.op}' op is not supported for XML array result. Use 'equal' op")
                }
                val valueSet = mutableSetOf<String>()
                for (i in 0 until nodes.length) {
                    valueSet.add(nodes.item(i).textContent)
                }
                assertEquals(
                    valueList.toSet(),
                    valueSet,
                    "XML path ${rule.path} validation failed, document: $document"
                )
            } else {
                val value = nodes.item(0).textContent
                when (rule.op) {
                    OpType.regex ->
                        assertThat(
                            value,
                            "XML path ${rule.path} validation failed, document: $document"
                        ).matches(Regex(valueList[0].toString()))
                    OpType.equal ->
                        assertEquals(
                            valueList[0],
                            value,
                            "XML path ${rule.path} validation failed, document: $document"
                        )
                    OpType.like ->
                        assertThat(
                            value,
                            "XML path ${rule.path} validation failed, document: $document"
                        ).matches(Regex(".*${valueList[0]}.*", RegexOption.DOT_MATCHES_ALL))
                }
            }
        }
    }
}

fun <T> assertk.Assert<T>.isXmlWhere(vararg validations: ValidationRule) = given { actual ->
    XmlPathValidator.assertXml(actual.toString(), validations.asList())
}

