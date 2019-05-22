import { Injectable } from '@angular/core';
import {Observable, of} from "rxjs";
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

  constructor(private http: HttpClient) { }

  user: User;
  isLoggedIn: boolean = false;

  // store the URL so we can redirect after logging in
  redirectUrl: string;

  login(email: string, authToken: string): Observable<any> {
    return this.http.post(loginEndpointUrl, {username: email, password: authToken}).pipe(
      map((data: any) => {
        console.log('Auth data', data);
      })
    );

    // return of(true).pipe(
    //   delay(1000),
    //   tap(val => this.isLoggedIn = true)
    // );

  }

  register(email: string): Observable<string> {
    return this.http.post(`${userEndpointUrl}/register`, {username: email, password: null}, {responseType: "text"}).pipe(
      map((message: string) => message)
    );
  }

  logout(): void {
    this.isLoggedIn = false;
  }
}
