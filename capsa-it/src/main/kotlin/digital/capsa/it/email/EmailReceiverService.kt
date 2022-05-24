package digital.capsa.it.email

import kotlin.jvm.Synchronized

import java.net.URLEncoder
import java.util.Properties
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage
import javax.mail.search.AndTerm
import javax.mail.search.FlagTerm
import javax.mail.search.NotTerm
import javax.mail.search.SearchTerm
import org.springframework.beans.factory.BeanFactory
import org.springframework.integration.mail.ImapMailReceiver
import org.springframework.integration.mail.SearchTermStrategy
import digital.capsa.core.logger
import java.nio.charset.StandardCharsets
import org.apache.tomcat.jni.Time
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EmailReceiverService(
    private val host: String,
    private val port: Int,
    private val isSsl: Boolean,
) {
    private val NUM_RETRYES = 50
    private val RETRY_TIMEOUT = 500L
    private val MAX_FETCH_SIZE = 500

    @Autowired
    private lateinit var beanFactory: BeanFactory

    @Synchronized()
    fun markEmailAsSeen(to: String) {
        val imapMailReceiver = imapMailReceiver(extractMainEmail(to))
        imapMailReceiver.receive()
    }

    @Synchronized()
    fun getEmail(
        to: String,
        numRetries: Int = NUM_RETRYES
    ): MimeMessage? {
        val email = extractMainEmail(to)
        logger.info("Trying to get email for $email, number of retries left is $numRetries")
        val imapMailReceiver = imapMailReceiver(email)
        try {
            val messages = imapMailReceiver.receive().filter { message ->
                message is MimeMessage && message.allRecipients.toList().map { it.toString() }.contains(to)
            }.map {
                it as MimeMessage
            }
            return if (messages.isNotEmpty()) {
                if (messages.size > 1) {
                    logger.warn("Found more than one email for $email, number of emails - ${messages.size}")
                }
                logger.info("Email for $to is found, subject is ${messages[0].subject}")
                messages[0]
            } else {
                if (numRetries <= 1) {
                    null
                } else {
                    Time.sleep(RETRY_TIMEOUT)
                    getEmail(
                        to = to,
                        numRetries = numRetries - 1
                    )
                }
            }
        } catch (e: MessagingException) {
            return getEmail(
                to = to,
                numRetries = numRetries - 1
            )
        }
    }

    private fun extractMainEmail(to: String): String {
        return to.replace(Regex("\\+\\S*\\@"), "@")
    }

    private fun imapMailReceiver(to: String): ImapMailReceiver {
        val url = "imap://${URLEncoder.encode(to, StandardCharsets.UTF_8)}" +
                ":${URLEncoder.encode("", StandardCharsets.UTF_8)}" +
                "@$host:$port/INBOX"
        logger.info("Initializing mailReceiver connecting with url $url")
        val mailReceiver = ImapMailReceiver(url)
        mailReceiver.setJavaMailProperties(javaMailImapProperties())
        mailReceiver.isShouldMarkMessagesAsRead = true
        mailReceiver.setSearchTermStrategy(UnreadSearchTermStrategy())
        mailReceiver.setMaxFetchSize(MAX_FETCH_SIZE)
        mailReceiver.setBeanFactory(beanFactory)
        mailReceiver.afterPropertiesSet()
        return mailReceiver
    }

    private fun javaMailImapProperties(): Properties {
        val javaMailProperties = Properties()

        javaMailProperties.setProperty("mail.imap.socketFactory.fallback", "false")
        javaMailProperties.setProperty("mail.store.protocol", "imap")
        if (isSsl) {
            javaMailProperties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        }
        javaMailProperties.setProperty("mail.debug", "false")

        return javaMailProperties
    }

    internal class UnreadSearchTermStrategy : SearchTermStrategy {
        override fun generateSearchTerm(supportedFlags: Flags?, folder: Folder): SearchTerm? {
            var searchTerm: SearchTerm? = null
            if (supportedFlags != null) {
                if (supportedFlags.contains(Flags.Flag.RECENT)) {
                    searchTerm = FlagTerm(Flags(Flags.Flag.RECENT), true)
                }
                if (supportedFlags.contains(Flags.Flag.SEEN)) {
                    val notSeen = NotTerm(FlagTerm(Flags(Flags.Flag.SEEN), true))
                    if (searchTerm == null) {
                        searchTerm = notSeen
                    } else {
                        searchTerm = AndTerm(searchTerm, notSeen)
                    }
                }
            }
            return searchTerm
        }
    }
}