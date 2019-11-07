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

export interface Team {
  id: number;
  name: string;
  description: string;
  email: string;
}

export interface ListTeamsResponse {
  message: string;
  teams: Team[];
}

export interface CreateTeamRequest {
  name: string;
}

export interface CreateTeamResponse {
  team: Team;
  message: string;
}

export interface DeleteTeamResponse {
  message: string;
}
