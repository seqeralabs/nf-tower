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

package io.seqera.tower.service.mail

import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Instant
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.seqera.mail.Mailer
import io.seqera.mail.MailerConfig
import io.seqera.mail.MailerStatus
import io.seqera.tower.domain.Mail
/**
 * Simple mail sender service
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class MailServiceImpl implements MailService {

    @Inject
    MailerConfig config

    BlockingQueue<Mail> pendingMails

    private volatile boolean terminated

    private volatile int errorCount

    private volatile int sentCount

    private String errorMessage

    private Instant errorTimestamp

    private Mailer mailer

    private long awaitMillis

    private Thread thread

    @PackageScope int getSentCount() { sentCount }
    
    @PackageScope int getErrorCount() { errorCount }
    
    @PostConstruct
    void init() {
        pendingMails = createMailQueue()
    }

    BlockingQueue<Mail> createMailQueue() {
        new LinkedBlockingQueue<Mail>()
    }

    @Override
    void sendMail(Mail mail) {
        assert mail, 'Mail object cannot be null'
        // create random UUID
        mail.id = UUID.randomUUID()
        // set from if missing
        if( !mail.from )
            mail.from(config.from)
        // add to pending queue
        pendingMails.add(mail)
    }

    protected sendLoop(dummy) {
        while(!terminated) {
            if( awaitMillis ) {
                sleep(awaitMillis); awaitMillis=0
            }

            takeAndSendMail0()
        }
    }

    protected void takeAndSendMail0() {
        Mail mail = null
        try {
            mail = pendingMails.take()
            log.debug "Mail sent to=${mail.to} subject=${mail.subject}"
            mailer.send(mail)
            sentCount +=1
            errorCount =0
        }
        catch (InterruptedException e) {
            log.warn("Mail service got interrupted", e)
            terminated = true
        }
        catch (Exception e) {
            // put back the mail message in the send queue to try it again
            if( mail )
                pendingMails.offer(mail)
            errorCount +=1
            errorMessage = e.message
            errorTimestamp = Instant.now()
            awaitMillis = 250 * Math.pow(3, errorCount) as long
            log.error("Unexpected error sending mail (await $awaitMillis ms) | ${e.message}")
        }
    }

    @Override
    void start() {
        log.info "+ Mail service started [${this.getClass().getSimpleName()}]"
        mailer = new Mailer() .setConfig(config)
        thread = Thread.startDaemon('Mailer thread',this.&sendLoop)
    }

    @Override
    void stop() {
        log.info "+ Mail service stopped"
        terminated = true
        thread.interrupt()
    }

    @Override
    MailerStatus getStatus() {
        def result = new MailerStatus(
                terminated: this.terminated,
                sentCount: this.sentCount,
                errorCount: this.errorCount,
                errorMessage: this.errorMessage,
                errorTimestamp: this.errorTimestamp,
                config: this.config.getMailProperties()
        )

        log.trace "Mailer status=$result"
        return result
    }


}
