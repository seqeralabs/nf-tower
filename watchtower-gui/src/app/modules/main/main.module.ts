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

import { MainRoutingModule } from './main-routing.module';
import { MainComponent } from './main.component';
import { SidebarComponent } from './component/sidebar/sidebar.component';
import { NavbarComponent } from './component/navbar/navbar.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import { WorkflowCardComponent } from './component/workflow-card/workflow-card.component';
import { WorkflowDetailComponent } from './component/workflow-detail/workflow-detail.component';
import { MulticoloredProgressBarComponent } from './component/multicolored-progress-bar/multicolored-progress-bar.component';
import { WelcomeComponent } from './component/welcome/welcome.component';
import { LoginComponent } from './component/login/login.component';
import {FormsModule} from "@angular/forms";
import { AuthComponent } from './component/auth/auth.component';
import {JwtInterceptor} from "./interceptor/jwt.interceptor";
import {ErrorInterceptor} from "./interceptor/error.interceptor";
import { LogoutComponent } from './component/logout/logout.component';
import { UserProfileComponent } from './component/user-profile/user-profile.component';
import { AccessTokenComponent } from "./component/access-token/access-token.component";
import { WorkflowTabsComponent } from "./component/workflow-tabs/workflow-tabs.component";
import { TasksTableComponent } from './component/tasks-table/tasks-table.component';
import { TasksProcessesComponent } from './component/tasks-processes/tasks-processes.component';
import { HomeComponent } from './component/home/home.component';
import { NotificationComponent } from './component/notification/notification.component';
import { LandingComponent } from './component/landing/landing.component';

@NgModule({
  declarations: [
    MainComponent,
    SidebarComponent,
    NavbarComponent,
    WorkflowCardComponent,
    WorkflowDetailComponent,
    MulticoloredProgressBarComponent,
    WelcomeComponent,
    LoginComponent,
    AuthComponent,
    LogoutComponent,
    UserProfileComponent,
    AccessTokenComponent,
    WorkflowTabsComponent,
    TasksTableComponent,
    TasksProcessesComponent,
    HomeComponent,
    NotificationComponent,
    LandingComponent
  ],
  imports: [
    BrowserModule,
    MainRoutingModule,
    HttpClientModule,
    FormsModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [MainComponent]
})
export class MainModule { }
