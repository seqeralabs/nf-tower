package io.seqera.watchtower.service

import java.security.Principal

import io.seqera.watchtower.domain.User

interface UserService {

    User register(String email)

    User update(Principal userSecurityData, User updatedUserData)

    void delete(Principal userSecurityData)

    User findByEmailAndAuthToken(String email, String token)

    User findByUserNameAndAccessToken(String userName, String token)

    List<String> findAuthoritiesByEmail(String email)

}
