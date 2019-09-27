package io.seqera.mail

import groovy.transform.InheritConstructors

/**
 * Exception thrown when the mail is sent more than once
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@InheritConstructors
class DuplicateMailSentException extends Exception {
}
