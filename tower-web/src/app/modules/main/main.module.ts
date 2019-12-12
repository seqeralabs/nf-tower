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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MainComponent } from './main.component';
import { SidebarComponent } from './component/sidebar/sidebar.component';
import { NavbarComponent } from './component/navbar/navbar.component';
import { HTTP_INTERCEPTORS, HttpClientModule } from "@angular/common/http";
import { WorkflowCardComponent } from './component/workflow-card/workflow-card.component';
import { WorkflowDetailComponent } from './component/workflow-detail/workflow-detail.component';
import { WorkflowMetricsComponent } from './component/workflow-metrics/metrics.component';
import { WelcomeComponent } from './component/welcome/welcome.component';
import { LoginComponent } from './component/login/login.component';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { JwtInterceptor } from "./interceptor/jwt.interceptor";
import { ErrorInterceptor } from "./interceptor/error.interceptor";
import { LogoutComponent } from './component/logout/logout.component';
import { UserProfileComponent } from './component/user-profile/user-profile.component';
import { AccessTokenComponent } from "./component/access-token/access-token.component";
import { TasksTableComponent } from './component/tasks-table/tasks-table.component';
import { TasksProcessesComponent } from './component/tasks-processes/tasks-processes.component';
import { HomeComponent } from './component/home/home.component';
import { NotificationComponent } from './component/notification/notification.component';
import { LandingComponent } from './component/landing/landing.component';
import { AuthGuard } from "./guard/auth.guard";
import { AuthComponent } from "./component/auth/auth.component";
import { BootstrapValidationCssDirective } from "./directive/bootstrap-validation";
import { WorkflowMainTabsComponent } from './component/workflow-main-tabs/workflow-main-tabs.component';
import { WorkflowGeneralComponent } from './component/workflow-general/workflow-general.component';
import { WorkflowStatusComponent } from './component/workflow-status/workflow-status.component';
import { WorkflowStatsComponent } from './component/workflow-stats/workflow-stats.component';
import { WorkflowUtilizationComponent } from './component/workflow-utilization/workflow-utilization.component';
import { ChartistModule } from "ng-chartist";
import { WorkflowLoadComponent } from './component/workflow-load/workflow-load.component';
import { WorkflowErrorComponent } from "./component/workflow-error/workflow-error.component";
import { WorkflowUnknownComponent } from "./component/workflow-unknown/workflow-unknown.component";
import { LoadingComponent } from './component/loading/loading.component';
import { TreeListComponent } from "./component/tree-list/TreeListComponent";
import { WorkflowStatusIconComponent } from "../../workflow-status-icon/workflow-status-icon.component";
import {WorkflowTopicComponent} from "./component/workflow-tags/workflow-topic.component";
import {AutocompleteLibModule} from 'angular-ng-autocomplete';

/*
 * Main application routing strategy
 */
const routes: Routes = [
  {path: '',                component: HomeComponent,
   children: [
     {path: 'watch/:id', component: WorkflowDetailComponent, canActivate: [AuthGuard]},
     {path: 'profile',      component: UserProfileComponent, canActivate: [AuthGuard]},
     {path: 'tokens',       component: AccessTokenComponent, canActivate: [AuthGuard]},
     {path: 'welcome',      component: WelcomeComponent, canActivate: [AuthGuard]},
     {path: 'login',        component: LoginComponent},
   ]
  },
  {path: 'metrics/:id',  component: WorkflowMetricsComponent},
  {path: 'auth',         component: AuthComponent},
  {path: 'logout',       component: LogoutComponent},

  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class MainRoutingModule { }

/*
 * Define the main application module
 */
@NgModule({
  declarations: [
    MainComponent,
    BootstrapValidationCssDirective,
    SidebarComponent,
    NavbarComponent,
    WorkflowCardComponent,
    WorkflowDetailComponent, WorkflowStatusIconComponent,
    WorkflowMainTabsComponent, WorkflowGeneralComponent, WorkflowStatusComponent,
    WorkflowStatsComponent, WorkflowUtilizationComponent, WorkflowLoadComponent, WorkflowErrorComponent, WorkflowUnknownComponent,
    WorkflowTopicComponent,
    WelcomeComponent,
    LoadingComponent,
    LoginComponent,
    AuthComponent,
    LogoutComponent,
    UserProfileComponent,
    AccessTokenComponent,
    WorkflowMetricsComponent,
    TasksTableComponent,
    TasksProcessesComponent,
    HomeComponent,
    NotificationComponent,
    LandingComponent,
    TreeListComponent
  ],
  imports: [
    BrowserModule,
    MainRoutingModule,
    HttpClientModule,
    FormsModule,
    ChartistModule,
    ReactiveFormsModule,
    AutocompleteLibModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [MainComponent]
})
export class MainModule { }

