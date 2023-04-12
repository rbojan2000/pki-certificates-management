import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Certificate } from 'src/app/model/certificate';

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  apiHost: string = 'http://localhost:8080/';
  headers: HttpHeaders = new HttpHeaders({
    'Content-Type': 'application/json',
  });

  constructor(private http: HttpClient) {}

  getCertificates(): Observable<Certificate[]> {
    return this.http.get<Certificate[]>(this.apiHost + 'api/certificate', {
      headers: this.headers,
    });
  }

  getCertificatesByUserId(): Observable<Certificate[]> {
    var userID = '642dafe9d9e299372bb8c612';
    return this.http.get<Certificate[]>(
      this.apiHost + 'api/certificate/getUserCertificates/' + userID,
      {
        headers: this.headers,
      }
    );
  }

  revokeCertificate(alias: string): any {
    return this.http.get(this.apiHost + 'api/certificate/revoke/' + alias, {
      headers: this.headers,
    });
  }

  createCertificate(createCertificateDTO: any) {
    return this.http.post<any>(
      this.apiHost + 'api/certificate/create',
      JSON.stringify(createCertificateDTO),
      {
        headers: this.headers,
      }
    );
  }

  createRootCertificate(certificateDTO: any) {
    return this.http.post<any>(
      this.apiHost + 'api/certificate/createRoot',
      JSON.stringify(certificateDTO),
      {
        headers: this.headers,
      }
    );
  }
}
