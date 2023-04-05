import { end, start } from '@popperjs/core';

export class Certificate {
  issuer: string;
  subject: string;
  startDate: string;
  endDate: string;
  type: string;

  constructor(
    issuer: string,
    subject: string,
    startDate: string,
    endDate: string,
    type: string
  ) {
    this.issuer = issuer;
    this.subject = subject;
    this.startDate = startDate;
    this.endDate = endDate;
    this.type = type;
  }
}
