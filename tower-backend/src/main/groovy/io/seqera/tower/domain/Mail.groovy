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

package io.seqera.tower.domain

import java.nio.file.Files
import java.nio.file.Path
import java.time.OffsetDateTime

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import io.seqera.util.CheckHelper
/**
 * Helper class modeling mail parameters
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@CompileDynamic
class Mail {

    static hasMany = [attachments: MailAttachment]

    String from

    String to

    String cc

    String bcc

    String subject

    String charset

    String body

    String text

    String type

    boolean sent

    OffsetDateTime dateCreated

    OffsetDateTime lastUpdated

    String lastError

    static mapping = {
        from(column: 'from_')
        to(column: 'to_')
        cc(column: 'cc_')
        bcc(column: 'bcc_')
        body(column: 'body_', type: 'text')
        text(column: 'text_', type: 'text')
        type(column: 'type_')
    }

    static constraints = {
        from(nullable: true, maxSize: 100)
        to(nullable: true, maxSize: 100)
        cc(nullable: true, maxSize: 100)
        bcc(nullable: true, maxSize: 100)
        subject(nullable: true, maxSize: 512)
        charset(nullable: true, maxSize: 20)
        text(nullable: true)
        type(nullable: true, maxSize: 50)
        lastError(nullable: true, lastError: 1024)
    }

    /**
     * Creates a {@link Mail} object given a {@link Mail} object
     *
     * @param params
     * @return A mail object representing the message to send
     */
    static Mail of(Map params) {
        def result = new Mail()

        if( params.from )
            result.from(params.from.toString())

        if( params.to )
            result.to(params.to.toString())

        if( params.cc )
            result.cc(params.cc.toString())

        if( params.bcc )
            result.bcc(params.bcc.toString())

        if( params.subject )
            result.subject(params.subject.toString())

        if( params.charset )
            result.charset(params.charset.toString())

        if( params.type )
            result.type(params.type.toString())

        if( params.body )
            result.body(params.body)

        if( params.text )
            result.text(params.text)

        if( params.attach )
            result.attach(params.attach)

        return result
    }

    /**
     * Mail sender (addresses must follow RFC822 syntax)
     * Multiple addresses can be separated by a comma
     */
    void from( String address ) {
        this.from = address
    }

    /**
     * Mail TO recipients (addresses must follow RFC822 syntax).
     * Multiple addresses can be separated by a comma.
     */
    void to( String address ) {
        this.to = address
    }

    /**
     * Mail CC recipients (addresses must follow RFC822 syntax).
     * Multiple addresses can be separated by a comma.
     */
    void cc( String address ) {
        this.cc = address
    }

    /**
     * Mail BCC recipients (addresses must follow RFC822 syntax).
     * Multiple addresses can be separated by a comma.
     */
    void bcc( String address ) {
        this.bcc = address
    }

    /**
     * @param subject The email subject
     */
    void subject( String subject ) {
        this.subject = subject
    }

    private String stringify( value ) {
        if( value instanceof File )
            return value.text
        if( value instanceof Path )
            return new String(Files.readAllBytes(value))
        if( value instanceof CharSequence )
            return value.toString()
        if( value != null )
            throw new IllegalArgumentException("Not a valid mail body argument [${value.getClass().getName()}]: $value")
        return null
    }

    /**
     * @param str The email content
     */
    void body( value ) {
        this.body = stringify(value)
    }

    /**
     * Plain text mail content
     * @param text The mail text content
     */
    void text( value ) {
        this.text = stringify(value)
    }

    /**
     * Mail content mime-type
     */
    void type( String mime )  {
        this.type = mime
    }

    /**
     * @param charset The mail content charset
     */
    void charset( String charset ) {
        this.charset = charset
    }

    /**
     * Add an email attachment
     *
     * @param item A attachment file path either as {@link File}, {@code Path} or {@link String} path
     */
    void attach( item ) {

        if( item instanceof MailAttachment ) {
            this.addToAttachments((MailAttachment)item)
        }
        else if( item instanceof Collection ) {
            for( def it : ((Collection)item) )
                this.addToAttachments(new MailAttachment(it))
        }
        else if( item instanceof Object[] ) {
            for( def it : ((Object[])item) )
                this.addToAttachments(new MailAttachment(it))
        }
        else if( item ) {
            this.addToAttachments(new MailAttachment(item))
        }
    }

    /**
     * Add an email attachment headers
     *
     * @param headers
     *      MailAttachment optional content directives. The following parameters are accepted:
     *      - contentId:  Set the "Content-ID" header field of this body part
     *      - fileName:  Set the filename associated with this body part, if possible
     *      - description: Set the "Content-Description" header field for this body part
     *      - disposition: Set the "Content-Disposition" header field of this body part
     *
     * @param item
     */
    void attach( Map headers, item ) {
        CheckHelper.checkParams('attach', headers, MailAttachment.ATTACH_HEADERS)

        if( this.attachments == null )
            this.attachments = []

        this.attachments << new MailAttachment(headers, item)
    }

    @Override
    String toString() {
        return "Mail[id=$id]"
    }
}
