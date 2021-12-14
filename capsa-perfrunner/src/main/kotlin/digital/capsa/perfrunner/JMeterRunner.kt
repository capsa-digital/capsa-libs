package digital.capsa.perfrunner

import com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup
import digital.capsa.perfrunner.domain.Plan
import org.apache.commons.io.FileUtils
import org.apache.jmeter.assertions.ResponseAssertion
import org.apache.jmeter.assertions.gui.AssertionGui
import org.apache.jmeter.control.TransactionController
import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.protocol.http.control.Header
import org.apache.jmeter.protocol.http.control.HeaderManager
import org.apache.jmeter.protocol.http.gui.HeaderPanel
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy
import org.apache.jmeter.protocol.http.util.HTTPArgument
import org.apache.jmeter.reporters.ResultCollector
import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.collections.ListedHashTree
import org.springframework.core.io.ClassPathResource
import java.io.File

object JMeterRunner {

    fun execute(plan: Plan): JMeterSummariser {
        val jmeter = StandardJMeterEngine()

        val jmeterPropertiesFile = File("jmeter-home/jmeter.properties")

        if (!jmeterPropertiesFile.exists()) {
            val inputStream = ClassPathResource("/jmeter-home/jmeter.properties", this::class.java).inputStream
            FileUtils.copyInputStreamToFile(inputStream, jmeterPropertiesFile)
        }

        JMeterUtils.setJMeterHome(jmeterPropertiesFile.path.replace("/jmeter.properties", ""))
        JMeterUtils.loadJMeterProperties(jmeterPropertiesFile.path)

        val rootTree = ListedHashTree()

        val testPlan = TestPlan(plan.name)
        val testPlanTree = rootTree.add(testPlan)

        for ((name, httpRequest, targetConcurrency, rampUpTime, rampUpStepCount, holdTargetRateTime,
            assertionResponseCode) in plan.groups) {

            val threadGroup = ConcurrencyThreadGroup()
            threadGroup.name = name
            threadGroup.targetLevel = targetConcurrency
            threadGroup.rampUp = rampUpTime
            threadGroup.steps = rampUpStepCount
            threadGroup.hold = holdTargetRateTime
            threadGroup.unit = "S"
            val threadGroupTree = testPlanTree.add(threadGroup)

            val headerManager = HeaderManager()
            headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager::class.java.name)
            headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel::class.java.name)
            httpRequest.headers?.map { (key, value) -> Header(key, value) }?.forEach(headerManager::add)
            threadGroupTree.add(headerManager)

            val transactionController = TransactionController()
            transactionController.isIncludeTimers = false
            val transactionControllerTree = threadGroupTree.add(transactionController)

            val httpSampler = HTTPSamplerProxy()
            httpSampler.method = httpRequest.method.name
            httpSampler.protocol = httpRequest.url.scheme
            httpSampler.domain = httpRequest.url.host
            httpRequest.url.port?.let { httpSampler.port = it }
            httpRequest.url.path?.let { httpSampler.path = it }
            if (!httpRequest.body.isNullOrEmpty()) {
                if (httpRequest.method.name in listOf("GET", "DELETE", "OPTIONS")) {
                    throw PerfRunnerException("${httpRequest.method.name} method does not support sending request body")
                } else {
                    httpSampler.postBodyRaw = true
                    val httpArgument = HTTPArgument()
                    httpArgument.name = "body"
                    httpArgument.value = httpRequest.body
                    httpSampler.arguments.addArgument(httpArgument)
                }
            }

            val httpSamplerTree = transactionControllerTree.add(httpSampler)

            val responseAssertion = ResponseAssertion()
            responseAssertion.setProperty(TestElement.TEST_CLASS, ResponseAssertion::class.java.name)
            responseAssertion.setProperty(TestElement.GUI_CLASS, AssertionGui::class.java.name)
            responseAssertion.assumeSuccess = false
            responseAssertion.setTestFieldResponseCode()
            responseAssertion.setScopeAll()
            responseAssertion.setToEqualsType()
            responseAssertion.addTestString(assertionResponseCode)
            httpSamplerTree.add(responseAssertion)
        }

        val summariser = JMeterSummariser()
        val logger = ResultCollector(summariser)
        testPlanTree.add(logger)

        jmeter.configure(rootTree)
        jmeter.run()

        return summariser
    }
}
