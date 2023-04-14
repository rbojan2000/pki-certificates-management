package pki.certificates.management.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CertificateDTO {
    public String subject;
    public String issuer;
    public String startDate;
    public String endDate;
    public String type;
    public String alias;
    public boolean isRevoked;

}
