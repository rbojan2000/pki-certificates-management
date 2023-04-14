import { end, start } from '@popperjs/core';

export class Certificate {
  issuer: string;
  subject: string;
  startDate: string;
  endDate: string;
  type: string;
  alias: string;
  isRevoked: boolean;

  constructor(
    issuer: string,
    subject: string,
    startDate: string,
    endDate: string,
    type: string,
    alias: string,
    isRevoked: boolean
  ) {
    this.issuer = issuer;
    this.subject = subject;
    this.startDate = startDate;
    this.endDate = endDate;
    this.type = type;
    this.alias = alias;
    this.isRevoked = isRevoked;
  }
}
