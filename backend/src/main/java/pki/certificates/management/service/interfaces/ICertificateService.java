package pki.certificates.management.service.interfaces;

import pki.certificates.management.dto.CertificateDto;
import pki.certificates.management.model.Issuer;

import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.List;

public interface ICertificateService {

    public List<CertificateDto> getAllCertificates();
    public List<CertificateDto> getCertificatesByAliases(List<String> aliases);
}
