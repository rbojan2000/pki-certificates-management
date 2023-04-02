import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreateKeyComponent } from './modules/create-key/create-key.component';
import { ViewComponent } from './modules/view/view.component';

const routes: Routes = [
  { path: 'create', component: CreateKeyComponent },
  { path: 'view', component: ViewComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
