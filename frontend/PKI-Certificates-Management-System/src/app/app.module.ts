import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './modules/navbar/navbar.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { CreateKeyComponent } from './modules/create-key/create-key.component';
import { ViewComponent } from './modules/view/view.component';
import { GenerateCertificateComponent } from './modules/generate-certificate/generate-certificate.component';

@NgModule({
  declarations: [AppComponent, NavbarComponent, CreateKeyComponent, ViewComponent, GenerateCertificateComponent],
  imports: [BrowserModule, NgbModule, AppRoutingModule],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
