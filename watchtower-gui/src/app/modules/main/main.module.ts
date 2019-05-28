import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { MainRoutingModule } from './main-routing.module';
import { MainComponent } from './main.component';
import { SidebarComponent } from './component/sidebar/sidebar.component';
import { HeaderComponent } from './component/header/header.component';
import { CentralComponent } from './component/central/central.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import { WorkflowCardComponent } from './component/workflow-card/workflow-card.component';
import { WorkflowDetailComponent } from './component/workflow-detail/workflow-detail.component';
import { MulticoloredProgressBarComponent } from './component/multicolored-progress-bar/multicolored-progress-bar.component';
import {MzDropdownModule, MzModalModule, MzSpinnerModule, MzTabModule, MzToastModule} from "ngx-materialize";
import { HomeComponent } from './component/home/home.component';
import { LoginComponent } from './component/login/login.component';
import {FormsModule} from "@angular/forms";
import { AuthComponent } from './component/auth/auth.component';
import {JwtInterceptor} from "./interceptor/jwt.interceptor";
import {ErrorInterceptor} from "./interceptor/error.interceptor";
import { LogoutComponent } from './component/logout/logout.component';
import { UserDetailComponent } from './component/user-detail/user-detail.component';
import { UserProfileComponent } from './component/user-profile/user-profile.component';
import {WorkflowTabsComponent} from "./component/workflow-tabs/workflow-tabs.component";
import { TasksTableComponent } from './component/tasks-table/tasks-table.component';

@NgModule({
  declarations: [
    MainComponent,
    SidebarComponent,
    HeaderComponent,
    CentralComponent,
    WorkflowCardComponent,
    WorkflowDetailComponent,
    MulticoloredProgressBarComponent,
    HomeComponent,
    LoginComponent,
    AuthComponent,
    LogoutComponent,
    UserDetailComponent,
    UserProfileComponent,
    WorkflowTabsComponent,
    TasksTableComponent,
  ],
  imports: [
    BrowserModule,
    MainRoutingModule,
    HttpClientModule,
    FormsModule,
    MzToastModule,
    MzSpinnerModule,
    MzDropdownModule,
    MzModalModule,
    MzTabModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [MainComponent]
})
export class MainModule { }
