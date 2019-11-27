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

package io.seqera.tower.exchange.captcha

import java.time.Instant

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Model Google Captcha response
 *
 * See https://developers.google.com/recaptcha/docs/verify#api_response
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
@ToString(includeNames = true)
class CaptchaVerifyResponse {
    boolean success
    Instant challenge_ts
    String hostname
}
