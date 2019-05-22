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
import {MzToastModule} from "ngx-materialize";
import { HomeComponent } from './component/home/home.component';
import { RegisterComponent } from './component/register/register.component';
import {FormsModule} from "@angular/forms";
import { LoginComponent } from './component/login/login.component';
import {JwtInterceptor} from "./interceptor/jwt.interceptor";

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
    LoginComponent
  ],
  imports: [
    BrowserModule,
    MainRoutingModule,
    HttpClientModule,
    FormsModule,
    MzToastModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
  ],
  bootstrap: [MainComponent]
})
export class MainModule { }
