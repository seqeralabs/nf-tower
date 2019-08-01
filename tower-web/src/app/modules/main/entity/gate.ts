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

export interface AccessGateResponse {
  message: string;
  state: AccessGateState;
}

export enum AccessGateState {
  LOGIN_ALLOWED,       // registration ok, sign-in email sent
  PENDING_APPROVAL,    // first time registration, login needs to be approved
  KEEP_CALM_PLEASE     // second time registration, login is not yet approved, just wait
}
