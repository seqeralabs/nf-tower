package io.seqera.mail

import java.time.Instant

/**
 * Model the current status for mailer object
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class MailerStatus implements Serializable, Cloneable {

    int sentCount
    int errorCount
    String errorMessage
    Instant errorTimestamp
    boolean paused
    boolean terminated
    long awaitDuration
    Properties config

}
