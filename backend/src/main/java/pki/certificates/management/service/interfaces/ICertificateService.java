package pki.certificates.management.service.interfaces;

import pki.certificates.management.model.Issuer;

import java.security.cert.X509Certificate;

public interface ICertificateService {
    public X509Certificate generateCertificate(Issuer issuer, String commonName, String organization, String organizationalUnit, String country, int validityDays);

}
