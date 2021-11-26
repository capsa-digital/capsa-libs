package digital.capsa.perfrunner

import org.apache.jmeter.reporters.Summariser
import org.apache.jmeter.samplers.SampleEvent
import org.apache.jmeter.visualizers.SamplingStatCalculator

@Suppress("TooManyFunctions")
open class JMeterSummariser : Summariser() {

    private val total = SamplingStatCalculator()

    override fun sampleOccurred(event: SampleEvent) {
        if (!event.isTransactionSampleEvent) {
            val result = event.result
            synchronized(total) {
                if (result != null) {
                    total.addSample(result)
                }
            }
        }
    }

    override fun sampleStarted(e: SampleEvent?) {}

    override fun sampleStopped(e: SampleEvent?) {}

    override fun testStarted() {}

    override fun testEnded() {}

    override fun testStarted(host: String?) {}

    override fun testEnded(host: String?) {}

    fun getNumSamples(): Long = total.count

    fun getAverage(): Long = total.elapsed / total.count

    fun getMedian(): Long = total.median.toLong()

    @Suppress("MagicNumber")
    fun getOnePercent(): Long = total.getPercentPoint(0.01).toLong()

    @Suppress("MagicNumber")
    fun getFivePercent(): Long = total.getPercentPoint(0.05).toLong()

    @Suppress("MagicNumber")
    fun getNintyFivePercent(): Long = total.getPercentPoint(0.95).toLong()

    @Suppress("MagicNumber")
    fun getNintyNineePercent(): Long = total.getPercentPoint(0.99).toLong()

    fun getErrorCount(): Long = total.errorCount

    fun printSummary(): String {
        val sb = StringBuilder()
        sb.append("Number of Requests raised=").append(total.count).append('\n')
        sb.append("Total Elapsed Time (ms)=").append(total.elapsed).append('\n')
        sb.append("Average Time (ms)=").append(getAverage()).append('\n')
        sb.append("Throughput rate (request/s)=").append(total.rate).append('\n')
        sb.append("Min Response Time (ms)=").append(total.min).append('\n')
        sb.append("1% Response Time (ms)=").append(getOnePercent()).append('\n')
        sb.append("5% Response Time (ms)=").append(getFivePercent()).append('\n')
        sb.append("Mean Response Time (ms)=").append(total.mean).append('\n')
        sb.append("Median Response Time (ms)=").append(total.median).append('\n')
        sb.append("95% Response Time (ms)=").append(getNintyFivePercent()).append('\n')
        sb.append("99% Response Time (ms)=").append(getNintyNineePercent()).append('\n')
        sb.append("Max Response Time (ms)=").append(total.max).append('\n')
        sb.append("ErrorCount=").append(total.errorCount).append('\n')
        sb.append("ErrorPercentage=").append(total.errorPercentage).append('\n')
        return sb.toString()
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}
