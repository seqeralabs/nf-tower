import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { MainRoutingModule } from './main-routing.module';
import { MainComponent } from './main.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { HeaderComponent } from './components/header/header.component';
import { CentralComponent } from './components/central/central.component';

@NgModule({
  declarations: [
    MainComponent,
    SidebarComponent,
    HeaderComponent,
    CentralComponent
  ],
  imports: [
    BrowserModule,
    MainRoutingModule,
  ],
  providers: [],
  bootstrap: [MainComponent]
})
export class MainModule { }
