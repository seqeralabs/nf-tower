/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.mail

import javax.activation.DataHandler
import javax.activation.URLDataSource
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.HeaderTokenizer
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility
import java.nio.charset.Charset
import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.seqera.tower.domain.Mail
import io.seqera.tower.domain.MailAttachment
import io.seqera.util.LogOutputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist
/**
 * This class implements the send mail functionality
 *
 * For API details see https://javaee.github.io/javamail/
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class Mailer {

    // Adapted from post by Phil Haack and modified to match better
    // See https://stackoverflow.com/a/22581832/395921
    private final static String TAG_START = "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)\\>"

    private final static String TAG_END = "\\</\\w+\\>"

    private final static String TAG_SELF_CLOSING = "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)/\\>"

    private final static String HTML_ENTITY = "&[a-zA-Z][a-zA-Z0-9]+;"

    private final static Pattern HTML_PATTERN = Pattern.compile("("+TAG_START+".*"+TAG_END+")|("+TAG_SELF_CLOSING+")|("+HTML_ENTITY+")", Pattern.DOTALL )

    private static String DEF_CHARSET = Charset.defaultCharset().toString()

    /**
     * Mail session object
     */
    private Session session

    /**
     * Holds mail settings and configuration attributes
     */
    private MailerConfig config = new MailerConfig()

    private Map env = System.getenv()

    Mailer setConfig(MailerConfig config) {
        this.config = config
        return this
    }

    MailerConfig getConfig() { config }

    /**
     * Get the properties of the system and insert the properties needed to the mailing procedure
     */
    protected Properties createProps() {
        def props = config.getMailProperties()
        // -- debug for debugging
        if( config.debug ) {
            log.debug "Mail session properties:\n${dumpProps(props)}"
        }
        else
            log.trace "Mail session properties:\n${dumpProps(props)}"
        return props
    }

    private String dumpProps(Properties props) {
        def dump = new StringBuilder()
        props.each {
            if( it.key.toString().contains('password') )
                dump << "  $it.key=xxx\n"
            else
                dump << "  $it.key=$it.value\n"
        }

        dump.toString()
    }

    /**
     * @return The mail {@link Session} object given the current configuration
     */
    protected Session getSession() {
        if( !session ) {
            session = Session.getInstance(createProps())
            if( config.debug != true )
                return session

            session.setDebugOut(new PrintStream( new LogOutputStream() {
                @Override protected void processLine(String line, int logLevel) {
                    log.debug(line)
                }
            } ))
            session.setDebug(true)
        }

        return session
    }

    /**
     * @return The SMTP host name or IP address
     */
    protected String getHost() {
        getConfig('host')
    }

    /**
     * @return The SMTP host port
     */
    protected int getPort() {
        def port = getConfig('port')
        port ? port as int : -1
    }

    /**
     * @return The SMTP user name
     */
    protected String getUser() {
        getConfig('user')
    }

    /**
     * @return The SMTP user password
     */
    protected String getPassword() {
        getConfig('password')
    }

    protected getConfig( String name ) {
        def props = config.getMailProperties()
        def key = "smtp.${name}"
        def value = props.getProperty("mail.$key")
        return value
    }

    /**
     * Send a email message by using the Java API
     *
     * @param message A {@link MimeMessage} object representing the email to send
     */
    protected void sendViaJavaMail(MimeMessage message) {
        if( !message.getAllRecipients() )
            throw new IllegalArgumentException("Missing mail message recipient")
        
        final transport = getSession().getTransport()
        log.debug("Connecting to host=$host port=$port user=$user")
        transport.connect(host, port as int, user, password)
        try {
            transport.sendMessage(message, message.getAllRecipients())
        }
        finally {
            transport.close()
        }
    }

    /**
     * @return A multipart mime message representing the mail message to send
     */
    protected MimeMessage createMimeMessage0(Mail mail) {

        final msg = new MimeMessage(getSession())

        if( mail.subject )
            msg.setSubject(mail.subject)

        if( mail.from )
            msg.addFrom(InternetAddress.parse(mail.from))
        else if( config.from )
            msg.addFrom(InternetAddress.parse(config.from.toString()))

        if ( config.to )
            msg.setRecipients(Message.RecipientType.TO, config.to)
        else if( mail.to )
            msg.setRecipients(Message.RecipientType.TO, mail.to)

        if( mail.cc )
            msg.setRecipients(Message.RecipientType.CC, mail.cc)

        if( mail.bcc )
            msg.setRecipients(Message.RecipientType.BCC, mail.bcc)

        return msg
    }


    /**
     * @return A multipart mime message representing the mail message to send
     */
    protected MimeMessage createMimeMessage(Mail mail) {

        final message = createMimeMessage0(mail)

        final wrap = new MimeBodyPart();
        final cover = new MimeMultipart("alternative")
        final charset = mail.charset ?: DEF_CHARSET

        if( mail.text ) {
            def part = new MimeBodyPart()
            part.setText(mail.text, charset)
            cover.addBodyPart(part)
        }

        if( mail.body ) {
            def part = new MimeBodyPart()
            String type = mail.type ?: guessMimeType(mail.body)
            if( !type.contains('charset=') )
                type = "$type; charset=${MimeUtility.quote(charset, HeaderTokenizer.MIME)}"
            part.setContent(mail.body, type)
            cover.addBodyPart(part)
        }
        wrap.setContent(cover)

        // use a separate multipart for body + attachment
        final content = new MimeMultipart("related");
        content.addBodyPart(wrap);

        // -- attachment
        def allFiles = mail.attachments ?: Collections.<MailAttachment>emptyList()
        for( MailAttachment item : allFiles ) {
            content.addBodyPart(createAttachment(item))
        }

        message.setContent(content);
        return message
    }

    protected MimeBodyPart createAttachment(MailAttachment item) {
        final result = new MimeBodyPart()
        if( item.file ) {
            if( !item.file.exists() )
                throw new MessagingException("The following attachment file does not exist: $item.file")
            result.attachFile(item.file)
        }
        else if( item.resource ) {
            def url = this.class.getResource(item.resource)
            if( !url )
                throw new MessagingException("The following attachment resource does not exist: $item.resource")
            def source = new URLDataSource(url)
            result.setDataHandler(new DataHandler(source))
        }
        else {
            throw new IllegalStateException("Invalid attachment object")
        }

        if( item.disposition )
            result.setDisposition(item.disposition)

        if( item.contentId )
            result.setContentID(item.contentId)

        if( item.fileName )
            result.setFileName(item.fileName)

        if( item.description )
            result.setDescription(item.description)

        return result
    }

    /**
     * Creates a pure text email message. It cannot contains attachments
     *
     * @param mail The {@link Mail} object representing the message to send
     * @return A {@link MimeMessage} object instance
     */
    @Deprecated
    protected MimeMessage createTextMessage(Mail mail) {
        final result = createMimeMessage0(mail)
        final charset = mail.charset ?: DEF_CHARSET
        final text = mail.text ?: stripHtml(mail.body)
        result.setText(text, charset)
        return result
    }

    /**
     * Converts an HTML text to a plain text message
     *
     * @param html The html string to strip
     */
    protected String stripHtml(String html) {
        if( !html )
            return html

        if( !guessHtml(html) )
            return html

        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        def result = Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        Parser.unescapeEntities(result, false)
    }

    protected boolean guessHtml(String str) {
        str ? HTML_PATTERN.matcher(str).find() : false
    }

    protected String guessMimeType(String str) {
        guessHtml(str) ? 'text/html' : 'text/plain'
    }

    /**
     * Send the mail given the provided config setting
     */
    void send(Mail mail) {
        log.trace "Mailer config: $config -- mail: $mail"
        def msg = createMimeMessage(mail)
        sendViaJavaMail(msg)
    }


    void sendAll(List<Mail> mails, Map<String,Closure> actions) {
        log.trace "Mailer config: $config -- mails count: ${mails.size()}"
        if( !mails ) {
            //nothing to do
            return
        }

        final transport = getTransport0()
        log.debug("Connecting to host=$host port=$port user=$user")
        transport.connect(host, port as int, user, password)
        try {
            for( Mail m : mails ) {
                createMessageAndSend0(transport, m, actions)
            }
        }
        finally {
            transport.close()
        }
    }

    protected Transport getTransport0() {
        getSession().getTransport()
    }

    protected void createMessageAndSend0(Transport transport, Mail mail, Map<String,Closure> actions ) {
        try {
            def msg = createMimeMessage(mail)
            transport.sendMessage(msg, msg.getAllRecipients())

            if( actions.containsKey('onSuccess'))
                actions.onSuccess.call(mail)
        }
        catch (Exception e){
            if( actions.containsKey('onError') ) {
                actions.onError.call(mail,e)
            }
            else
                throw e
        }
    }

    /**
     * Send a mail given a parameter map
     *
     * @param params
     *      The following named parameters are supported
     *      - from: the email sender address
     *      - to: the email recipient address
     *      - cc: the CC recipient address
     *      - bcc: the BCC recipient address
     *      - subject: the email subject
     *      - charset: the email content charset
     *      - type: the email body mime-type
     *      - text: the email plain text alternative content
     *      - body: the email body content (HTML)
     *      - attach: he email attachment
     */
    void send(Map params) {
        send(Mail.of(params))
    }


    /**
     * Send a mail message using a closure to fetch the required parameters
     *
     * @param params
     *    A closure representing the mail message to send eg
     *    <code>
     *        sendMail {
     *          to 'me@dot.com'
     *          from 'your@name.com'
     *          attach '/some/file/path'
     *          subject 'Hello'
     *          body '''
     *           Hi there,
     *           Hope this email find you well
     *          '''
     *        }
     *    <code>
     */
    void send( Closure params ) {
        def mail = new Mail()
        def copy = (Closure)params.clone()
        copy.setResolveStrategy(Closure.DELEGATE_FIRST)
        copy.setDelegate(mail)
        def body = copy.call(mail)
        if( !mail.body && (body instanceof String || body instanceof GString))
            mail.body(body)
        send(mail)
    }

}
