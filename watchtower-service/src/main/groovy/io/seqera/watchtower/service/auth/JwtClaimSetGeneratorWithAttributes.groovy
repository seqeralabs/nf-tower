package io.seqera.watchtower.service.auth

import com.nimbusds.jwt.JWTClaimsSet
import io.micronaut.context.annotation.Primary
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.token.config.TokenConfiguration
import io.micronaut.security.token.jwt.generator.claims.ClaimsAudienceProvider
import io.micronaut.security.token.jwt.generator.claims.JWTClaimsSetGenerator
import io.micronaut.security.token.jwt.generator.claims.JwtIdGenerator

import javax.annotation.Nullable
import javax.inject.Singleton

@Primary
@Singleton
class JwtClaimSetGeneratorWithAttributes extends JWTClaimsSetGenerator {

    JwtClaimSetGeneratorWithAttributes(TokenConfiguration tokenConfiguration, @Nullable JwtIdGenerator jwtIdGenerator, @Nullable ClaimsAudienceProvider claimsAudienceProvider, @Nullable ApplicationConfiguration applicationConfiguration) {
        super(tokenConfiguration, jwtIdGenerator, claimsAudienceProvider, applicationConfiguration)
    }

    JwtClaimSetGeneratorWithAttributes(TokenConfiguration tokenConfiguration, @Nullable JwtIdGenerator jwtIdGenerator, @Nullable ClaimsAudienceProvider claimsAudienceProvider) {
        super(tokenConfiguration, jwtIdGenerator, claimsAudienceProvider)
    }

    protected void populateWithUserDetails(JWTClaimsSet.Builder builder, UserDetails userDetails) {
        super.populateWithUserDetails(builder, userDetails)

        userDetails.getAttributes('roles', 'userName').each { String propertyName, def propertyValue ->
            builder.claim(propertyName, propertyValue)
        }
    }

}
