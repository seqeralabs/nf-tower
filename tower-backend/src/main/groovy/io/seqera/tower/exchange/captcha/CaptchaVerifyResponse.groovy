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
