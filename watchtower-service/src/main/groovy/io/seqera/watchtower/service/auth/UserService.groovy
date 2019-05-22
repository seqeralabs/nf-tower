package io.seqera.watchtower.service.auth

import io.seqera.watchtower.domain.auth.User

interface UserService {

    User register(String email)

    User findByEmailAndAuthToken(String username, String authToken)

    List<String> findAuthoritiesByEmail(String email)

}
