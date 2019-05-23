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
import {MzDropdownModule, MzNavbarModule, MzSpinnerModule, MzToastModule} from "ngx-materialize";
import { HomeComponent } from './component/home/home.component';
import { RegisterComponent } from './component/register/register.component';
import {FormsModule} from "@angular/forms";
import { LoginComponent } from './component/login/login.component';
import {JwtInterceptor} from "./interceptor/jwt.interceptor";
import {ErrorInterceptor} from "./interceptor/error.interceptor";
import { LogoutComponent } from './component/logout/logout.component';

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
    RegisterComponent,
    LoginComponent,
    LogoutComponent,
  ],
  imports: [
    BrowserModule,
    MainRoutingModule,
    HttpClientModule,
    FormsModule,
    MzToastModule,
    MzSpinnerModule,
    MzDropdownModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [MainComponent]
})
export class MainModule { }
