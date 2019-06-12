package io.seqera.watchtower.service

import java.security.Principal

import io.seqera.watchtower.domain.User

interface UserService {

    User register(String email)

    User getFromAuthData(Principal userSecurityData)

    User update(User existingUser, User updatedUserData)

    void delete(User existingUser)

    User findByEmailAndAuthToken(String email, String token)

    User findByUserNameAndAccessToken(String userName, String token)

    List<String> findAuthoritiesByEmail(String email)

    List<String> findAuthoritiesOfUser(User user)

}
