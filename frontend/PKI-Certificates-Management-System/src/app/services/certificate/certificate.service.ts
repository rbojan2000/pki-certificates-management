import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Certificate } from 'src/app/model/certificate';

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  apiHost: string = 'http://localhost:8081/';
  headers: HttpHeaders = new HttpHeaders({
    'Content-Type': 'application/json',
  });

  constructor(private http: HttpClient) {}

  getCertificates(): Observable<Certificate[]> {
    return this.http.get<Certificate[]>(this.apiHost + 'certificates/getAll', {
      headers: this.headers,
    });
  }
}
