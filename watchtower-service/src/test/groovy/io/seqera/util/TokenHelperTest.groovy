package io.seqera.util


import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class TokenHelperTest extends Specification {

    def 'should create random token' () {
        when:
        def tkn1 = TokenHelper.createHexToken()
        def tkn2 = TokenHelper.createHexToken()
        then:
        tkn1.length() == 40
        tkn2.length() == 40
        tkn1 != tkn2
    }

}

