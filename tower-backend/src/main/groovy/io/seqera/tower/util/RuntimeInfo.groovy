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

package io.seqera.tower.util

import groovy.transform.CompileStatic

/**
 * Helper class to find out runtime info
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class RuntimeInfo {

    static String getOsName() { System.getProperty('os.name') }
    static String getOsVersion() { System.getProperty('os.version') }
    static String getGroovyVersion() { GroovySystem.getVersion() }
    static String getJvmName() { System.getProperty('java.vm.name') }
    static String getJvmVersion() { System.getProperty('java.runtime.version') }
    static String getFileEncoding() { System.getProperty('file.encoding') }
    static String getFileNameEncoding() { System.getProperty('sun.jnu.encoding') }

    static String info(String newline='\n') {
        final result = new StringBuilder()
        result << "System: ${osName} ${osVersion}" << newline
        result << "Runtime: Groovy ${groovyVersion} on ${jvmName} ${jvmVersion}" << newline
        result << "Encoding: ${fileEncoding} (${fileNameEncoding})" << newline
        return result.toString()
    }

}
