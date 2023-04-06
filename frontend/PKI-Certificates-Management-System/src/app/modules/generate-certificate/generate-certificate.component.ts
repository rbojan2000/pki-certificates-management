import { Component, OnInit, SimpleChanges } from '@angular/core';
import { Certificate } from 'src/app/model/certificate';
import { CertificateService } from 'src/app/services/certificate/certificate.service';

@Component({
  selector: 'app-generate-certificate',
  templateUrl: './generate-certificate.component.html',
  styleUrls: ['./generate-certificate.component.css']
})

export class GenerateCertificateComponent implements OnInit {
  
  public certificates: any[] = [];
  public selectedTemp: any;
  public selectedCN: any;
  public selectedO: any;
  public selectedUN: any;
  public selecltedOU: any;
  public selectedC: any;
  public selectedST: any;
  public selectedStartDate: any;
  public selectedEndDate: any;
  public selectedKeySize: any;
  
  public subjectCN: any;
  public subjectO: any;
  public subjectUN: any;
  public subjectCountry: any;
  public startDate: any;
  public endDate: any;
  

  constructor(private certificateService: CertificateService) {}

  ngOnInit(): void {
    this.certificateService.getCertificatesByUserId("12").subscribe((res: any) => {
      this.certificates = res;
    });

  }

  chaged()
 {
  window.alert(this.subjectCN)
 }
  findCertificateByAlias(alias: string): Certificate | undefined {
    return this.certificates.find(cert => cert.alias === alias);
  }


  onSelectionChanged() {

  const selectedAlias = this.selectedKeySize;
  const selectedCert = this.findCertificateByAlias(selectedAlias);


  if (selectedCert) {
    const certSubject = selectedCert.subject;
    const subjectObj: any = {
      C: '',
      ST: '',
      L: '',
      OU: '',
      O: '',
      CN: ''
    };

    certSubject.split(',').forEach((item: string) => {
      const parts = item.trim().split('=');
      subjectObj[parts[0]] = parts[1];
    });


    this.selectedCN = subjectObj.CN; // access the CN value
    this.selectedC = subjectObj.C;
    this.selectedEndDate = this.findCertificateByAlias(this.selectedKeySize)?.endDate;
    this.selectedStartDate = this.findCertificateByAlias(this.selectedKeySize)?.startDate;
    this.selectedO = subjectObj.O;
    this.selecltedOU = subjectObj.OU;
    this.selectedUN = subjectObj.UN;
    this.selectedST = subjectObj.ST;
    this.selectedTemp = this.findCertificateByAlias(this.selectedKeySize)?.type;
  }
}
  
  
}
