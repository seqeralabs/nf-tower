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

import java.nio.file.Path

import groovy.transform.EqualsAndHashCode
/**
 * Model a mail attachment
 */
@EqualsAndHashCode
class MailAttachment implements Serializable {

    public static final Map ATTACH_HEADERS = [
            contentId: String,
            disposition:String,
            fileName: String,
            description: String
    ]

    static belongsTo = [mail: Mail]

    /**
     * The attachment file
     */
    File file

    /**
     * The attachment as classpath resource
     */
    String resource

    String contentId

    String description

    String disposition

    String fileName

    static MailAttachment resource(Map params, String path) {
        def result = new MailAttachment(params)
        result.@resource = path
        return result
    }

    protected MailAttachment() {}

    MailAttachment(attach ) {
        this([:], attach)
    }

    MailAttachment(Map params ) {
        if( params.contentId )
            this.contentId = params.contentId

        if( params.description )
            this.description = params.description

        if( params.disposition )
            this.disposition = params.disposition

        if( params.fileName )
            this.fileName = params.fileName
    }

    MailAttachment(Map params, attach ) {
        this(params)

        if( attach instanceof File ) {
            this.file = attach
        }
        else if( attach instanceof Path ) {
            this.file = attach.toFile()
        }
        else if( attach instanceof String || attach instanceof GString ) {
            this.file = new File(attach.toString())
        }
        else if( attach != null )
            throw new IllegalArgumentException("Invalid attachment argument: $attach [${attach.getClass()}]")

    }

    File getFile() { file }

    String getResource() { resource }

    String getFileName() {
        if( fileName )
            return fileName

        if( file )
            return file.getName()

        if( resource ) {
            def str = resource
            def p = str.indexOf(':')
            if( p!=-1 )
                str = resource.substring(p+1)
            p = str.lastIndexOf('/')
            if( p!=-1 )
                str = str.substring(p+1)
            return str
        }

        return null
    }
}
