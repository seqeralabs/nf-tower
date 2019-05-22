import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, of, Subject} from "rxjs";
import {delay, map, tap} from "rxjs/operators";
import {User} from "../entity/user/user";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../../environments/environment";

const loginEndpointUrl: string = `${environment.apiUrl}/login`;
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

  get currentUser(): User {
    return this.userSubject.value
  }


  login(email: string, authToken: string): Observable<User> {
    return this.http.post(loginEndpointUrl, {username: email, password: authToken}).pipe(
      map((authData: any) => {
        return <User> {email: authData.username, accessToken: authData['access_token'], roles: authData.roles}
      }),
      tap((user: User) => {
        this.persistUser(user);
        this.userSubject.next(user);
      })
    );
  }

  register(email: string): Observable<string> {
    return this.http.post(`${userEndpointUrl}/register`, {username: email, password: null}, {responseType: "text"}).pipe(
      map((message: string) => message)
    );
  }

  logout(): void {
    this.removeUser();
    this.userSubject.next(null);
  }

  private persistUser(user: User): void {
    localStorage.setItem('user', JSON.stringify(user));
  }

  private getPersistedUser(): User {
    return <User> JSON.parse(localStorage.getItem('user'));
  }

  private removeUser(): void {
    localStorage.removeItem('user');
  }
}
