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

package io.seqera.annotation


/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class VerFSTTest extends GroovyTestCase {

    void testVersionAnnot () {
        assertScript '''
        import io.seqera.annotation.VerFST
        import org.nustaq.serialization.annotations.Version
        
        class Foo {
            @VerFST(10) String bar  
        }
        
        def field = Foo.class.getDeclaredField('bar')
        def annot = field.getAnnotation(Version)
        assert annot.value() == 10
    '''
    }
}
