package io.seqera.tower.service

import io.micronaut.context.annotation.Factory
import org.springframework.context.MessageSource
import org.springframework.context.support.AbstractResourceBasedMessageSource
import org.springframework.context.support.ResourceBundleMessageSource

import javax.inject.Singleton

@Factory
class MessageSourceFactory {

    @Singleton
    MessageSource messageSource() {
        AbstractResourceBasedMessageSource messageSource = new ResourceBundleMessageSource()
        messageSource.basename = 'i18n/messages'

        messageSource
    }

}
