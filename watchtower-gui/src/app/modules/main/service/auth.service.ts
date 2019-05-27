import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, of, Subject} from "rxjs";
import {delay, map, tap} from "rxjs/operators";
import {User} from "../entity/user/user";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../../environments/environment";
import {UserData} from "../entity/user/user-data";

const authEndpointUrl: string = `${environment.apiUrl}/login`;
const userEndpointUrl: string = `${environment.apiUrl}/user`;

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  user$: Observable<User>;

  private userSubject: BehaviorSubject<User>;

  constructor(private http: HttpClient) {
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
      map((authData: any) => this.retrieveUserFromAuthResponse(authData)),
      tap((user: User) => this.setAuthUser(user))
    );
  }

  private retrieveUserFromAuthResponse(authData: any) {
    let userData: UserData = <UserData> {email: authData.username, accessToken: authData['access_token'], roles: authData.roles};

    let attributes = this.parseJwt(userData.accessToken);
    userData.userName = attributes.userName;
    userData.firstName = attributes.firstName;
    userData.lastName = attributes.lastName;
    userData.organization = attributes.organization;
    userData.description = attributes.description;
    userData.avatar = attributes.avatar;

    return new User(userData);
  }

  private setAuthUser(user: User): void {
    this.persistUser(user);
    this.userSubject.next(user);
  }

  register(email: string): Observable<string> {
    return this.http.post(`${userEndpointUrl}/register`, {username: email, password: null}, {responseType: "text"}).pipe(
      map((message: string) => message)
    );
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

  logout(): void {
    this.removeUser();
    this.userSubject.next(null);
  }

  private parseJwt(token: string): any {
    let base64Url = token.split('.')[1];
    let decodedBase64 = decodeURIComponent(atob(base64Url).split('')
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
