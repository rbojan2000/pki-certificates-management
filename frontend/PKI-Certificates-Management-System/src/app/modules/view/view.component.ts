import { Component, OnInit } from '@angular/core';
import { Certificate } from 'src/app/model/certificate';
import { CertificateService } from 'src/app/services/certificate/certificate.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.css'],
})
export class ViewComponent implements OnInit {
  public certificates: Certificate[] = [];

  constructor(
    private certificateService: CertificateService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.certificateService.getCertificates().subscribe((res: any) => {
      this.certificates = res;
    });
  }

  revokeCertificate(alias: string) {
    this.certificateService.revokeCertificate(alias).subscribe();
    location.reload();
  }

  validateCertificate(alias: string) {
    this.certificateService.validateCertificate(alias).subscribe((res) => {
      if (res) {
        this.toastr.success('Valid!');
      } else {
        this.toastr.error('Invalid!');
      }
    });
  }
}
