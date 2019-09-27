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

import javax.inject.Inject
import javax.inject.Singleton
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.seqera.tower.domain.Mail
import io.seqera.tower.service.MailService
import io.seqera.util.LRUCache
import org.springframework.transaction.annotation.Propagation
/**
 * Implements mail spooler operations
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class MailSpoolerImpl implements MailSpooler {

    private LRUCache<Long, Boolean> sentMails = new LRUCache<>(10_000)

    @Inject
    MailerConfig mailerConfig

    @Inject
    MailService mailService

    final int shortSleepMillis = 250

    final int longSleepMillis = 30_000

    private volatile long awaitDurationMillis = longSleepMillis

    private volatile  long lastMailSignal

    private volatile boolean terminated

    private volatile boolean paused

    private Thread executor

    private Lock sync

    private Condition avail

    private Mailer mailer

    private int penaltyCount

    private int sentCount

    private int errorCount

    private String errorMessage

    private Instant errorTimestamp

    @Value('${tower.contact-email}')
    String contactEmail

    protected void run() {
        log.info "Mail spooler started"
        mailer = new Mailer() .setConfig(mailerConfig)
        sync = new ReentrantLock()
        avail = sync.newCondition()

        while(!terminated) {
            try {
                await()
                if(!status.paused) {
                    sendPendingEmails()
                }
            }
            catch (InterruptedException e) {
                log.warn("Mail spooler got interrupted", e)
                terminated = true
            }
            catch ( DuplicateMailSentException e ) {
                this.status.paused = true
                log.error(e.message)
                mailer.send(duplicateMailMessage(e.message))
            }
            catch (Exception e) {
                penaltyCount += 1
                status.errorCount +=1
                status.errorMessage = e.message
                status.errorTimestamp = Instant.now()
                awaitDurationMillis = getAwaitDuration()
                log.error("Unexpected error sending mail | ${e.message}")
            }
        }

        log.info "Mail spooler terminated"
    }

    protected void await() {
        sync.lock()
        try {
            if( awaitDurationMillis>longSleepMillis )
                log.debug "Spooler await duration=${awaitDurationMillis /1_000}s"
            final ret = avail.await(awaitDurationMillis, TimeUnit.MILLISECONDS)
            log.trace "Spooler await exit=$ret"
        }
        finally {
            sync.unlock()
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected sendPendingEmails() {
        final found = new ArrayList<>(mailService.findPendingMails())
        if( found )
            log.debug "Pending mail to send: ${found.size()}"

        // double check none of this was already sent
        final itr = found.iterator()
        while( itr.hasNext() )  {
            final m = itr.next()
            if( sentMails.containsKey(m.id) ) {
                throw new DuplicateMailSentException("Mail already sent [id=${m.id}; to=${m.to}]")
            }
        }

        int errors=0
        final actions = new HashMap(2)
        actions.onSuccess = { Mail mail ->
            mail.sent = true
            safeSave(mail)
            this.sentCount += 1
            this.sentMails.put(mail.id, true)
            log.debug "Mail sent to=${mail.to} subject=${mail.subject} sent-cont=${this.status.sentCount}"
        }
        actions.onError = { Mail mail, Exception e ->
            final msg = "Error sending email [id=$mail.id to=${mail.to}] | ${e.message}"
            log.warn(msg)
            errors++
            mail.lastError = e.message
            errorMessage = msg
            errorCount += 1
            errorTimestamp = Instant.now()
            safeSave(mail)
        }

        mailer.sendAll(found, actions)

        // increase the penalty count
        penaltyCount = errors ? penaltyCount+1 : 0

        if( (found.size()==0  && System.currentTimeMillis()-lastMailSignal>1_000) || errors>0 ) {
            awaitDurationMillis = getAwaitDuration()
        }

    }

    protected void safeSave(Mail m) {
        try {
            m.save(failOnError:true)
        }
        catch( Exception e ) {
            log.error("Unable to save mail [id=${m.id}; to=${m.to}]", e)
        }
    }

    @Override
    void start() {
        executor = Thread.start('Mail spooler', this.&run)
    }

    @Override
    void stop() {
        terminated = true
        signal0()
    }

    @Override
    void newMail() {
        log.trace "Got mail spooler signal"
        lastMailSignal = System.currentTimeMillis()
        awaitDurationMillis = shortSleepMillis
        signal0()
    }

    private void signal0() {
        sync.lock()
        try {
            avail.signal()
        }
        finally {
            sync.unlock()
        }
    }

    @Override
    void pause(boolean value) {
        log.info "Mail spooler setting pause=$value"
        if(value) {
            // enter in paused mode
            paused = true
        }
        else {
            // exit from paused mode
            errorCount = 0
            paused = false
            signal0()
        }
    }

    @Override
    MailerStatus getStatus() {
        new MailerStatus(
                paused: this.paused,
                terminated: this.terminated,
                sentCount: this.sentCount,
                errorCount: this.errorCount,
                errorMessage: this.errorMessage,
                errorTimestamp: this.errorTimestamp,
                awaitDuration: this.awaitDurationMillis,
                config: this.mailerConfig.getMailProperties()
        )
    }

    private Mail duplicateMailMessage(String msg) {
        def mail = new Mail()
        mail.to(contactEmail)
        mail.subject('Duplicate mail error')
        mail.text = msg
        return mail
    }

    protected long getAwaitDuration() {
        longSleepMillis * Math.pow(2, penaltyCount) as long
    }

}
