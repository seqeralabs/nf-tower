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
import {WorkflowTabsComponent} from "./component/workflow-tabs/workflow-tabs.component";
import { TasksTableComponent } from './component/tasks-table/tasks-table.component';
import { TasksProcessesComponent } from './component/tasks-processes/tasks-processes.component';
import { HomeComponent } from './component/home/home.component';
import { NotificationComponent } from './component/notification/notification.component';

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
    WorkflowTabsComponent,
    TasksTableComponent,
    TasksProcessesComponent,
    HomeComponent,
    NotificationComponent,
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
