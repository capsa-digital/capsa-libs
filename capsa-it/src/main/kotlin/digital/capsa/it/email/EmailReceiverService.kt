package digital.capsa.it.email

import digital.capsa.core.logger
import org.apache.tomcat.jni.Time
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.mail.ImapMailReceiver
import org.springframework.integration.mail.SearchTermStrategy
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage
import javax.mail.search.AndTerm
import javax.mail.search.FlagTerm
import javax.mail.search.NotTerm
import javax.mail.search.SearchTerm

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

    fun getEmails(
        to: String,
        subject: String? = null,
        numRetries: Int = NUM_RETRYES
    ): List<MimeMessage> {
        logger.info("Trying to get email for $to, number of retries left is $numRetries")

        for (i in 0 until numRetries) {
            if (i != 0) {
                Time.sleep(RETRY_TIMEOUT)
            }

            val messages =
                try {
                    getEmails(to = to, subject = subject)
                } catch (e: MessagingException) {
                    emptyList()
                }

            if (messages.isNotEmpty()) {
                return messages
            }
        }

        return emptyList()
    }

    @Synchronized()
    private fun getEmails(
        to: String,
        subject: String? = null
    ): List<MimeMessage> {
        val email = extractMainEmail(to)
        val imapMailReceiver = imapMailReceiver(email)
        val messages = imapMailReceiver.receive().filter { message ->
            message is MimeMessage && message.allRecipients.toList().map { it.toString() }.contains(to)
                    && if (subject != null) message.subject.toString().contains(subject) else true
        }.map {
            it as MimeMessage
        }

        if (messages.size > 1) {
            logger.warn("Found more than one email for $email, number of emails - ${messages.size}")
        }
        logger.info("Email for $email is found, subject is ${messages[0].subject}")

        return messages
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