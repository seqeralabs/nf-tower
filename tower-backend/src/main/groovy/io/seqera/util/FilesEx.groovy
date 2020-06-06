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

package io.seqera.util

import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.InvokerHelper

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class FilesEx {

    static String toUriString(Path path ) {
        if(path==null)
            return null
        final scheme = getScheme(path)
        if( scheme == 'file' )
            return path.toString()
        if( scheme == 's3' )
            return "$scheme:/$path".toString()
        if( scheme == 'gs' ) {
            final bucket = InvokerHelper.invokeMethod(path, 'bucket', InvokerHelper.EMPTY_ARGS)
            return "$scheme://$bucket$path".toString()
        }
        return path.toUri().toString()
    }

    static String getScheme(Path path) {
        path.getFileSystem().provider().getScheme()
    }

    static void closeQuietly( Closeable self ) {
        try {
            if(self) self.close()
        }
        catch (IOException ioe) {
            log.debug "Exception closing $self -- Cause: ${ioe.getMessage() ?: ioe.toString()}"
        }
    }
    
}
