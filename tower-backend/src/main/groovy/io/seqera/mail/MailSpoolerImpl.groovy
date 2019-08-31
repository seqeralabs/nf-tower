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
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.seqera.tower.domain.Mail
import io.seqera.tower.service.MailService
import org.springframework.transaction.annotation.Propagation
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class MailSpoolerImpl implements MailSpooler {

    @Inject
    MailerConfig mailerConfig

    @Inject
    MailService mailService

    final int shortSleepMillis = 250

    final int longSleepMillis = 30_000

    private volatile long awaitDurationMillis = longSleepMillis

    private volatile  long lastMailSignal

    private volatile boolean stopped

    private Thread executor

    private Lock sync

    private Condition avail

    protected void run() {
        log.info "Mail spooler started"

        sync = new ReentrantLock()
        avail = sync.newCondition()

        while(!stopped) {
            try {
                await()
                checkMail()
            }
            catch (InterruptedException e) {
                log.warn("Mail spooler got interrupted", e)
                stopped = true
            }
            catch (Exception e) {
                log.error("Unexpected error sending mail", e)
            }
        }

        log.info "Mail spooler terminated"
    }

    protected void await() {
        sync.lock()
        try {
            final ret = avail.await(awaitDurationMillis, TimeUnit.MILLISECONDS)
            log.trace "Spooler exit await=$ret"
        }
        finally {
            sync.unlock()
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected checkMail() {
        final mailer = new Mailer() .setConfig(mailerConfig)

        final found = mailService.findPendingMails()
        log.debug "Pending mail to send: ${found.size()}"

        int errors=0
        final actions = new HashMap(2)
        actions.onSuccess = { Mail mail ->
            mail.sent = true; safeSave(mail)
        }
        actions.onError = { Mail mail, Exception e ->
            log.warn "Failed to send mail with ID=$mail.id", e
            errors++
            mail.lastError = e.message
            safeSave(mail)
        }

        mailer.sendAll(found, actions)

        if( (found.size()==0  && System.currentTimeMillis()-lastMailSignal>1_000) || errors>0 )
            awaitDurationMillis = longSleepMillis
    }

    protected void safeSave(Mail m) {
        try {
            m.save(failOnError:true)
        }
        catch( Exception e ) {
            log.error("Unexpected error saving mail with ID=${m.id}", e)
        }
    }

    @Override
    void start() {
        executor = Thread.start('Mail spooler', this.&run)
    }

    @Override
    void stop() {
        stopped = true
        avail.signal()
    }

    @Override
    void newMail() {
        lastMailSignal = System.currentTimeMillis()
        awaitDurationMillis = shortSleepMillis
        sync.lock()
        try {
            log.trace "Got mail spooler signal"
            avail.signal()
        }
        finally {
            sync.unlock()
        }
    }
}
