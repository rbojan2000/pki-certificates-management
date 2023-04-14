import { Component, OnInit } from '@angular/core';
import { CertificateService } from 'src/app/services/certificate/certificate.service';
import { UserService } from 'src/app/services/certificate/user.service';

@Component({
  selector: 'app-generate-root-certificate',
  templateUrl: './generate-root-certificate.component.html',
  styleUrls: ['./generate-root-certificate.component.css'],
})
export class GenerateRootCertificateComponent implements OnInit {
  public subjectCN: any;
  public subjectO: any;
  public subjectOU: any;
  public subjectUN: any;
  public subjectCountry: any;
  public startDate: any;
  public endDate: any;
  public user: any;

  constructor(
    private userService: UserService,
    private certificateService: CertificateService
  ) {}

  ngOnInit(): void {
    this.userService.getUserByID().subscribe((res: any) => {
      this.user = res;
      this.subjectO = this.user.organisation;
      this.subjectOU = this.user.unit;
      this.subjectCountry = this.user.state;
    });
  }

  createCertificate() {
    const certificateDTO: any = {
      userID: this.user.id,
      subjectCN: this.subjectCN,
      subjectO: this.subjectO,
      subjectOU: this.subjectOU,
      subjectCountry: this.subjectCountry,
      startDate: this.startDate,
      endDate: this.endDate,
      selectedAuthority: 'true',
    };

    let startDateMs = Date.parse(this.startDate);
    let endDateMs = Date.parse(this.endDate);

    if (endDateMs < startDateMs || startDateMs < Date.now() - 86400000) {
      window.alert('Wrong date!');
    } else {
      this.certificateService.createRootCertificate(certificateDTO).subscribe();
    }
  }
}
