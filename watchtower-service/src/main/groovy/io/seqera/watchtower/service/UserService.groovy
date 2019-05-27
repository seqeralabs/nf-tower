package io.seqera.watchtower.service

import io.micronaut.security.authentication.Authentication
import io.seqera.watchtower.domain.User

import java.security.Principal

interface UserService {

    User register(String email)

    User update(Principal userSecurityData, User updatedUserData)

    void delete(Principal userSecurityData)

    User findByEmailAndAuthToken(String username, String authToken)

    List<String> findAuthoritiesByEmail(String email)

}
