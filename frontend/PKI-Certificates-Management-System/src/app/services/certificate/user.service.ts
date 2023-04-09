import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class UserService {
  apiHost: string = 'http://localhost:8080/';
  headers: HttpHeaders = new HttpHeaders({
    'Content-Type': 'application/json',
  });

  constructor(private http: HttpClient) {}

  getUserByID(): Observable<any> {
    var userID = '642dafe9d9e299372bb8c612';
    return this.http.get<any>(
      this.apiHost + 'api/user/getUser/' + userID,
      {
        headers: this.headers,
      }
    );
  }
  

}
