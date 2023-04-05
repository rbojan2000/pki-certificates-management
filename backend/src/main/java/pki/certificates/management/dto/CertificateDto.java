package pki.certificates.management.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
public class CertificateDto {
    public String subject;
    public String issuer;
    public String startDate;
    public String endDate;

    public String type;


}
