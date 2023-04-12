package pki.certificates.management.service.interfaces;

import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import pki.certificates.management.dto.CertificateDTO;
import pki.certificates.management.dto.CreateCertificateDTO;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.List;

public interface ICertificateService {

    public List<CertificateDTO> getAllCertificates();
    public List<CertificateDTO> getCertificatesByAliases(List<String> aliases);
    public X509Certificate createEndEntityOrIntermediateCertificate(CreateCertificateDTO createCertificateDTO) throws  IOException, CertificateException, OperatorCreationException, ParseException;
    public X509Certificate createRootCertificate(CreateCertificateDTO createCertificateDTO) throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, ParseException, CertIOException, CertificateException;

    List<CertificateDTO> userCertificates(String userID);

    public void revokeCertificate(String alias);
}
