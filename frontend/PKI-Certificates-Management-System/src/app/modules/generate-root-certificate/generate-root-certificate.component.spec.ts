import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenerateRootCertificateComponent } from './generate-root-certificate.component';

describe('GenerateRootCertificateComponent', () => {
  let component: GenerateRootCertificateComponent;
  let fixture: ComponentFixture<GenerateRootCertificateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GenerateRootCertificateComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenerateRootCertificateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
