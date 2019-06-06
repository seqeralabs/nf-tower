import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {WorkflowDetailComponent} from "./component/workflow-detail/workflow-detail.component";
import {WelcomeComponent} from "./component/welcome/welcome.component";
import {LoginComponent} from "./component/login/login.component";
import {AuthGuard} from "./guard/auth.guard";
import {AuthComponent} from "./component/auth/auth.component";
import {LogoutComponent} from "./component/logout/logout.component";
import {UserProfileComponent} from "./component/user-profile/user-profile.component";
import {MainComponent} from "./main.component";
import {HomeComponent} from "./component/home/home.component";

const routes: Routes = [
  {path: '',                component: HomeComponent,
   children: [
     {path: 'workflow/:id', component: WorkflowDetailComponent},
     {path: 'profile',      component: UserProfileComponent, canActivate: [AuthGuard]}
   ]
  },
  {path: 'auth',         component: AuthComponent},
  {path: 'login',        component: LoginComponent},
  {path: 'logout',       component: LogoutComponent},

  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class MainRoutingModule { }
