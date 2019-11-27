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

import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
class MailAttachmentTest extends AbstractContainerBaseTest {

    void 'should create resource attachment' () {
        when:
        MailAttachment attach = MailAttachment.resource('foo/bar', contentId: 'the-cid')

        then:
        attach.file == null
        attach.resource == 'foo/bar'
        attach.contentId == 'the-cid'

    }

    void 'should crate attachment'  () {
        given:
        MailAttachment attach

        when:
        attach = new MailAttachment('/some/path/foo.png')
        then:
        attach.file == new File('/some/path/foo.png')
        attach.fileName == 'foo.png'
        attach.contentId == null
        attach.description == null
        attach.disposition == null

        when:
        attach = new MailAttachment('/some/path/foo.png', contentId: 'id-1', description: 'Hola', fileName: 'bar.png', disposition: 'inline')
        then:
        attach.file == new File('/some/path/foo.png')
        attach.fileName == 'bar.png'
        attach.description == 'Hola'
        attach.contentId == 'id-1'
        attach.disposition == 'inline'

        when:
        attach = MailAttachment.resource('jar:/some/path/foo.png', contentId: '<foo>')
        then:
        attach.file == null
        attach.resource == 'jar:/some/path/foo.png'
        attach.fileName == 'foo.png'
        attach.contentId == '<foo>'

        when:
        attach = MailAttachment.resource([:], 'jar:foo.png')
        then:
        attach.file == null
        attach.resource == 'jar:foo.png'
        attach.fileName == 'foo.png'

    }

}
