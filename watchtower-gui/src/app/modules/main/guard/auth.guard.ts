import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthService} from "../service/auth.service";
import {NotificationService} from "../service/notification.service";

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService,
              private notificationService: NotificationService,
              private router: Router) {}

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    let url: string = state.url;

    return this.checkLogin();
  }

  checkLogin(): boolean {
    if (this.authService.isUserLoggedIn) {
      return true;
    }

    console.log('User not authenticated');
    this.notificationService.showErrorNotification('Please log in');
    this.router.navigate(['/']);
    return false;
  }
  
}
