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
export interface AccessToken {
    id: number;
    token: string;
    name: string;
    dateCreated: Date;
    lastUsed: Date;
}

export interface CreateAccessTokenRequest {
  name: string;
}

export interface CreateAccessTokenResponse {
    token: AccessToken;
    message: string;
}

export interface ListAccessTokensResponse {
  tokens: AccessToken[];
  message: string;
}

