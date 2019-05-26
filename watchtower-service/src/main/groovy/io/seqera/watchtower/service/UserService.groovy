package io.seqera.watchtower.service

import io.seqera.watchtower.domain.User

interface UserService {

    User register(String email)

    User findByEmailAndAuthToken(String username, String authToken)

    List<String> findAuthoritiesByEmail(String email)

}
