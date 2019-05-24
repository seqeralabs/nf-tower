package io.seqera.mail

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.ConfigurationProperties
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@ConfigurationProperties('mail')
@CompileDynamic
class MailerConfig {

    String to
    String from
    Map<String,?> smtp = [auth: false]
    Map<String,?> transport = Collections.emptyMap()
    boolean debug

    Properties getMailProperties() {
        def props = new Properties()
        for( Map.Entry<String,?> entry : smtp ) {
            props.setProperty('mail.smtp.' + entry.key, entry.value?.toString() )
        }

        props.setProperty('mail.transport.protocol', transport.protocol  ?: 'smtp')

        // -- check proxy configuration
        if( !props.contains('mail.smtp.proxy.host') && sysProp('http.proxyHost') ) {
            props['mail.smtp.proxy.host'] = sysProp('http.proxyHost')
            props['mail.smtp.proxy.port'] = sysProp('http.proxyPort')
        }

        // -- debug for debugging
        if( debug ) {
            log.debug "Mail session properties:\n${props.dump()}"
        }
        else
            log.trace "Mail session properties:\n${props.dump()}"

        return props
    }

    protected String sysProp(String key) {
        System.getProperty(key)
    }
}
