import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GenerateCertificateComponent } from './modules/generate-certificate/generate-certificate.component';
import { ViewComponent } from './modules/view/view.component';

const routes: Routes = [
  { path: 'view', component: ViewComponent },
  { path: 'generateCertificate', component: GenerateCertificateComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
