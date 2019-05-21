package io.seqera.watchtower.service.auth

import io.seqera.watchtower.domain.auth.User

interface UserService {

    User register(String email)

    List<String> findAuthoritiesByUsername(String username)

}
