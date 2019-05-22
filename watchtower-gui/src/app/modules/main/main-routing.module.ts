import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {WorkflowDetailComponent} from "./component/workflow-detail/workflow-detail.component";
import {HomeComponent} from "./component/home/home.component";
import {RegisterComponent} from "./component/register/register.component";
import {AuthGuard} from "./guard/auth.guard";
import {LoginComponent} from "./component/login/login.component";

const routes: Routes = [
  {path: '',             component: HomeComponent, canActivate: [AuthGuard]},
  {path: 'register',        component: RegisterComponent},
  {path: 'login',        component: LoginComponent},
  {path: 'workflow/:id', component: WorkflowDetailComponent},

  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class MainRoutingModule { }
