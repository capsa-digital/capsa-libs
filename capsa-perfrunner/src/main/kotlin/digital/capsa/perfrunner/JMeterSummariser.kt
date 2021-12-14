package digital.capsa.perfrunner

import org.apache.jmeter.reporters.Summariser
import org.apache.jmeter.samplers.SampleEvent
import org.apache.jmeter.visualizers.SamplingStatCalculator

class JMeterSummariser : Summariser() {

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

    fun getAverage(): Double = total.elapsed * 1.0 / total.count

    fun getMedian(): Long = total.median.toLong()

    fun getOnePercent(): Long = total.getPercentPoint(0.01).toLong()

    fun getFivePercent(): Long = total.getPercentPoint(0.05).toLong()

    fun getNintyFivePercent(): Long = total.getPercentPoint(0.95).toLong()

    fun getNintyNineePercent(): Long = total.getPercentPoint(0.99).toLong()

    fun getErrorCount(): Long = total.errorCount

    fun getSummary(): String =
        StringBuilder()
            .append("Number of Requests raised = ").append(total.count).append('\n')
            .append("Total Elapsed Time = ").append(total.elapsed).append(" ms\n")
            .append("Throughput rate = ").append("%.2f".format(total.rate)).append(" requests/s\n")
            .append("Min Response Time = ").append(total.min).append(" ms\n")
            .append("1% Response Time = ").append(getOnePercent()).append(" ms\n")
            .append("5% Response Time = ").append(getFivePercent()).append(" ms\n")
            .append("Average Response Time = ").append("%.2f".format(getAverage())).append(" ms\n")
            .append("Mean Response Time = ").append("%.2f".format(total.mean)).append(" ms\n")
            .append("Median Response Time = ").append(total.median).append(" ms\n")
            .append("95% Response Time = ").append(getNintyFivePercent()).append(" ms\n")
            .append("99% Response Time = ").append(getNintyNineePercent()).append(" ms\n")
            .append("Max Response Time = ").append(total.max).append(" ms\n")
            .append("Error Count = ").append(total.errorCount).append('\n')
            .append("Error Percentage = ").append("%.2f".format(total.errorPercentage * 100)).append("%\n")
            .toString()

    companion object {
        private const val serialVersionUID = 0L
    }
}
