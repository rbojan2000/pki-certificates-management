import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GenerateCertificateComponent } from './modules/generate-certificate/generate-certificate.component';
import { GenerateRootCertificateComponent } from './modules/generate-root-certificate/generate-root-certificate.component';
import { MyCertificatesComponent } from './modules/my-certificates/my-certificates.component';
import { ViewComponent } from './modules/view/view.component';

const routes: Routes = [
  { path: 'view', component: ViewComponent },
  { path: 'myCertificates', component: MyCertificatesComponent },
  { path: 'generateCertificate', component: GenerateCertificateComponent },
  { path: 'generateRoot', component: GenerateRootCertificateComponent },
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
