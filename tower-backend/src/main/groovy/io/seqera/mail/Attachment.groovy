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

import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
/**
 * Model a mail attachment
 */
@CompileStatic
@ToString(includeNames = true)
@EqualsAndHashCode
class Attachment {

    public static final Map ATTACH_HEADERS = [
            contentId: String,
            disposition:String,
            fileName: String,
            description: String
    ]

    /**
     * The attachment file
     */
    private File file

    /**
     * The attachment as classpath resource
     */
    private String resource

    /**
     * Attachment content parameters
     */
    private Map params

    static Attachment resource(Map params, String path) {
        def result = new Attachment()
        result.@resource = path
        result.@params = params != null ? params : [:]
        return result
    }

    protected Attachment() {}

    Attachment( attach ) {
        this([:], attach)
    }

    Attachment( Map params, attach ) {
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

        this.params = params != null ? params : [:]
    }

    File getFile() { file }

    String getResource() { resource }

    String getContentId() { params.contentId }

    String getDescription() { params.description }

    String getDisposition() { params.disposition }

    String getFileName() {
        if( params.fileName )
            return params.fileName

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