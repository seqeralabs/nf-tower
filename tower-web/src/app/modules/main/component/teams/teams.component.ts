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

import {Component, OnInit} from "@angular/core";
import {environment} from "src/environments/environment";
import {
  CreateTeamRequest,
  CreateTeamResponse,
  DeleteTeamResponse,
  ListTeamsResponse,
  Team
} from "../../entity/team-entities";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";
import {Router} from "@angular/router";

@Component({
  selector: 'wt-team',
  templateUrl: './teams.component.html',
  styleUrls: ['./teams.component.scss'],
})
export class TeamsComponent implements OnInit {
  teams: Team[];
  disabled: boolean;

  constructor(private httpClient: HttpClient,
              private notificationService: NotificationService,
              private router: Router) {

  }

  ngOnInit(): void {
    const url = `${environment.apiUrl}/team`;
    this.httpClient.get<ListTeamsResponse>(url)
      .subscribe(
        resp => {
          this.teams = resp.teams;
          this.disabled = this.teams.length===0;
        },
        (resp: HttpErrorResponse) => {
          this.notificationService.showErrorNotification(resp.error.message);
        }
      );
  }

  createNewTeam() {
    const name = prompt('Specify the team name');
    if( !name ) {
      return;
    }

    const url = `${environment.apiUrl}/team`;
    this.httpClient.post<CreateTeamResponse>(url, {name})
      .subscribe(
        resp => {
          this.teams.push(resp.team);
          this.disabled = this.teams.length===0;
        },
        (resp: HttpErrorResponse) => {
          this.notificationService.showErrorNotification(resp.error.message);
        }
      );
  }

  deleteTeam(team: Team) {
    const confirm = prompt(`Please confirm the deletion of the team '${team.name}' typing its name below (operation is not recoverable):`);
    if( confirm !== team.name )
      return;

    const url = `${environment.apiUrl}/team/${team.id}`;
    this.httpClient.delete<DeleteTeamResponse>(url)
      .subscribe(
        resp => {
          const index = this.teams.indexOf(team);
          this.teams.splice(index, 1);
          this.disabled = this.teams.length===0;
        },
        (resp: HttpErrorResponse) => {
          this.notificationService.showErrorNotification(resp.error.message);
        }
      );
  }
}
