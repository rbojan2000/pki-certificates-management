package pki.certificates.management.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateCertificateDTO {
    public String aliasIssuer;
    public String subjectCN;
    public String subjectO;
    public String subjectOU;
    public String subjectUN;
    public String subjectCountry;
    public String startDate;
    public String endDate;
    public String selectedAuthority;
    public String userID;
}
