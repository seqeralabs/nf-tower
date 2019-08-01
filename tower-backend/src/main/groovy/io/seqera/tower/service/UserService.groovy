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

package io.seqera.tower.service

import java.security.Principal

import io.seqera.tower.domain.User

interface UserService {

    User create(String email, String authority)

    User generateAuthToken(User user)

    User getFromAuthData(Principal userSecurityData)

    User update(User existingUser, User updatedUserData)

    void delete(User existingUser)

    User findByEmailAndAuthToken(String email, String token)

    @Deprecated
    User findByUserNameAndAccessToken(String userName, String token)

    List<String> findAuthoritiesByEmail(String email)

    List<String> findAuthoritiesOfUser(User user)

    User getByAccessToken(String token)

}
