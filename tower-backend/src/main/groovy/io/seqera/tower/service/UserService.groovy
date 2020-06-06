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

    User create(String email)

    User getOrCreate(String email)

    User updateUserAuthToken(User user)

    User getByAuth(Principal principal)

    User getByEmail(String email)

    User getByUid(String uid)

    User update(User existingUser, User updatedUserData)

    void delete(User existingUser)

    @Deprecated
    User findByEmailAndAuthToken(String email, String token)

    User findByUidAndAuthToken(String uid, String token)

    List<User> list()

    List<User> list(int offset)

    List<User> list(int offset, int max)

    @Deprecated
    List<String> findAuthoritiesByEmail(String email)

    List<String> findAuthoritiesByUser(User user)

    List<String> findAuthoritiesByUid(String uid)

    User getByAccessToken(String token)

    boolean updateLastAccessTime(Long userId)

}
