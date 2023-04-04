import { AfterViewInit, Component } from '@angular/core';
import { Certificate } from 'src/app/model/certificate';
import { CertificateService } from 'src/app/services/certificate/certificate.service';

@Component({
  selector: 'app-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.css'],
})
export class ViewComponent implements AfterViewInit {
  public certificates: Certificate[] = [];

  //private certificateService: CertificateService inject in constructor
  constructor() {
    const certificate1 = new Certificate(
      'Issuer 1',
      'Subject 1',
      '11.1.2022.',
      '12.7.2024.'
    );
    const certificate2 = new Certificate(
      'Issuer 2',
      'Subject 2',
      '23.4.2021.',
      '15.6.2023.'
    );
    const certificate3 = new Certificate(
      'Issuer 3',
      'Subject 3',
      '25.1.2022.',
      '18.12.2025.'
    );

    const certificate4 = new Certificate(
      'Issuer 4',
      'Subject 4',
      '25.1.2022.',
      '18.12.2025.'
    );

    const certificate5 = new Certificate(
      'Issuer 5',
      'Subject 5',
      '25.1.2022.',
      '18.12.2025.'
    );

    const certificate6 = new Certificate(
      'Issuer 6',
      'Subject 6',
      '25.1.2022.',
      '18.12.2025.'
    );

    this.certificates.push(
      certificate1,
      certificate2,
      certificate3,
      certificate4,
      certificate5,
      certificate6
    );
  }

  ngAfterViewInit(): void {
    // this.certificateService.getCertificates().subscribe((res: any) => {
    //   this.certificates = res;
    // });
  }
}
