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
import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, of, Subject} from "rxjs";
import {map, mergeMap, tap} from "rxjs/operators";
import {User} from "../entity/user/user";
import {HttpClient} from "@angular/common/http";
import {environment} from "src/environments/environment";
import {UserData} from "../entity/user/user-data";
import {AccessGateResponse} from "../entity/gate";
import {Router} from "@angular/router";

const authEndpointUrl: string = `${environment.apiUrl}/login`;
const userEndpointUrl: string = `${environment.apiUrl}/user`;
const gateEndpointUrl: string = `${environment.apiUrl}/gate`;

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  user$: Observable<User>;

  private userSubject: BehaviorSubject<User>;

  constructor(private http: HttpClient,
              private router: Router) {
    this.userSubject = new BehaviorSubject(this.getPersistedUser());
    this.user$ = this.userSubject.asObservable();
  }

  get isUserAuthenticated(): boolean {
    return (this.currentUser != null);
  }

  get currentUser(): User {
    return this.userSubject.value
  }


  auth(email: string, authToken: string): Observable<User> {
    return this.http.post(authEndpointUrl, {username: email, password: authToken}).pipe(
      mergeMap((authData: any) => this.requestUserProfileInfo(authData)),
      tap((user: User) => this.setAuthUser(user))
    );
  }

  private requestUserProfileInfo(authData: any): Observable<User> {
    const userData: UserData = <UserData> {email: authData.username, jwtAccessToken: authData['access_token'], roles: authData.roles};

    return this.http.get(`${userEndpointUrl}/`, {headers: {'Authorization': `Bearer ${userData.jwtAccessToken}`}}).pipe(
      map((data: any) => {
        userData.id = data.user.id;
        userData.userName = data.user.userName;
        userData.firstName = data.user.firstName;
        userData.lastName = data.user.lastName;
        userData.organization = data.user.organization;
        userData.description = data.user.description;
        userData.avatar = data.user.avatar;
        userData.notification = data.user.notification;
        return new User(userData);
      })
    );
  }

  private setAuthUser(user: User): void {
    this.persistUser(user);
    this.userSubject.next(user);
  }

  access(email: string, captcha: string): Observable<AccessGateResponse> {
    return this.http.post<AccessGateResponse>(`${gateEndpointUrl}/access`, {email: email, captcha: captcha});
  }

  update(user: User): Observable<string> {
    return this.http.post(`${userEndpointUrl}/update`, user.data, {responseType: "text"}).pipe(
      map((message: string) => message),
      tap( () => this.setAuthUser(user)),
    );
  }

  delete(): Observable<string> {
    return this.http.delete(`${userEndpointUrl}/delete`, {responseType: "text"}).pipe(
      map((message: string) => message)
    );
  }

  logoutAndGoHome() {
    this.logout();
    this.router.navigate(['/']);
  }

  private logout(): void {
    this.removeUser();
    this.userSubject.next(null);
  }

  private parseJwt(token: string): any {
    const base64Url = token.split('.')[1];
    const decodedBase64 = decodeURIComponent(atob(base64Url).split('')
      .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join(''));

    return JSON.parse(decodedBase64);
  };

  private persistUser(user: User): void {
    localStorage.setItem('user', JSON.stringify(user.data));
  }

  private getPersistedUser(): User {
    const userData: UserData = <UserData> JSON.parse(localStorage.getItem('user'));

    return (userData ? new User(userData) : null);
  }

  private removeUser(): void {
    localStorage.removeItem('user');
  }
}
