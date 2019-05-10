import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { MainRoutingModule } from './main-routing.module';
import { MainComponent } from './main.component';
import { SidebarComponent } from './component/sidebar/sidebar.component';
import { HeaderComponent } from './component/header/header.component';
import { CentralComponent } from './component/central/central.component';
import {HttpClientModule} from "@angular/common/http";
import { WorkflowCardComponent } from './component/workflow-card/workflow-card.component';
import { WorkflowDetailComponent } from './component/workflow-detail/workflow-detail.component';
import { MulticoloredProgressBarComponent } from './component/multicolored-progress-bar/multicolored-progress-bar.component';

@NgModule({
  declarations: [
    MainComponent,
    SidebarComponent,
    HeaderComponent,
    CentralComponent,
    WorkflowCardComponent,
    WorkflowDetailComponent,
    MulticoloredProgressBarComponent
  ],
  imports: [
    BrowserModule,
    MainRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [MainComponent]
})
export class MainModule { }
