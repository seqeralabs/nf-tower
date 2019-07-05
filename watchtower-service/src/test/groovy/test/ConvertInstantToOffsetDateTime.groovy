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

package test

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.regex.Matcher
//assert args
// the json file to parse

def replaceLine(String line) {
    Matcher m = line =~ /"(submit|start|complete)": "(\d{4}.*Z)"/
    if( m.find() ) {
        def v = m.group(2)
        def x = v=='0' || v=='null' ? 'null' :  OffsetDateTime.ofInstant(Instant.parse(m.group(2)), ZoneId.systemDefault()).toString()

        def prefix = line[0..m.start(1)-1]
        def middle = line[m.end(1)..(m.start(2)-1)]
        def postix = line[m.end(2)..line.size()-1]

        return "${prefix}${m.group(1)}${middle}${x}${postix}"
    }
    else
        return line
}



assert args
//def name = '/Users/pditommaso/Projects/nf-tower/watchtower-service/src/test/resources/workflow_success/2_task_1_submitted.json'
name = args[0]
def file = new File(name)
def count=0
def newFile = new File("${name}.new")
println "Processing file=$file"
newFile.withWriter {
    for( String line : file.readLines() ) {
        def newLine = replaceLine(line)
        it << newLine << '\n'
        if( newLine != line )
            count++
    }
}

if( count ) {
    def bak = new File("${name}.bak")
    file.renameTo(bak)
    newFile.renameTo(new File(name))
}
else {
    newFile.delete()
}




