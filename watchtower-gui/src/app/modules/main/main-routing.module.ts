import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {WorkflowDetailComponent} from "./component/workflow-detail/workflow-detail.component";
import {HomeComponent} from "./component/home/home.component";
import {LoginComponent} from "./component/login/login.component";
import {AuthGuard} from "./guard/auth.guard";
import {AuthComponent} from "./component/auth/auth.component";
import {LogoutComponent} from "./component/logout/logout.component";

const routes: Routes = [
  {path: '',             component: HomeComponent,
    children: [
      {path: 'workflow/:id', component: WorkflowDetailComponent, canActivate: [AuthGuard]}
    ]
  },
  {path: 'login',     component: LoginComponent},
  {path: 'auth',        component: AuthComponent},
  {path: 'logout',       component: LogoutComponent},

  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class MainRoutingModule { }
