import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {WorkflowDetailComponent} from "./component/workflow-detail/workflow-detail.component";
import {HomeComponent} from "./component/home/home.component";
import {RegisterComponent} from "./component/register/register.component";
import {AuthGuard} from "./guard/auth.guard";
import {LoginComponent} from "./component/login/login.component";
import {LogoutComponent} from "./component/logout/logout.component";

const routes: Routes = [
  {path: '',             component: HomeComponent,
    children: [
      {path: 'workflow/:id', component: WorkflowDetailComponent, canActivate: [AuthGuard]}
    ]
  },
  {path: 'register',     component: RegisterComponent},
  {path: 'login',        component: LoginComponent},
  {path: 'logout',       component: LogoutComponent},

  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class MainRoutingModule { }
