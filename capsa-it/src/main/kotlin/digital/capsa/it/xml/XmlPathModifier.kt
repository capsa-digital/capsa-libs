package digital.capsa.it.xml

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


object XmlPathModifier {

    init {
        setIdeaIoUseFallback()
    }

    fun modifyXml(xml: String, transformationData: Map<String, Any?>): String {

        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml)))

        val xpath: XPath = XPathFactory.newInstance().newXPath()
        for ((path, value) in transformationData) {
            val nodes: NodeList = xpath.evaluate(
                path, document,
                XPathConstants.NODESET
            ) as NodeList

            for (idx in 0 until nodes.length) {
                nodes.item(idx).textContent = value.toString()
            }
        }

        val tf = TransformerFactory.newInstance()
        val transformer: Transformer = tf.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        val writer = StringWriter()
        transformer.transform(DOMSource(document), StreamResult(writer))
        return writer.buffer.toString().trimEnd()
    }
}