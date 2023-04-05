package pki.certificates.management.service.implementations;

import org.springframework.stereotype.Service;
import pki.certificates.management.dto.CertificateDto;
import pki.certificates.management.service.interfaces.ICertificateService;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class CertificateService implements ICertificateService {

    @Override
    public List<CertificateDto> getAllCertificates() {
        List<Certificate> certificates = getAllCertificatesFromKeyStores();
        return mapCertificatesToDtos(certificates, null);
    }

    @Override
    public List<CertificateDto> getCertificatesByAliases(List<String> aliases) {
        List<Certificate> certificates = getCertificatesByAliasesFromKeyStores(aliases);
        return mapCertificatesToDtos(certificates, aliases);
    }

    private List<Certificate> getAllCertificatesFromKeyStores() {
        List<Certificate> certificates = new ArrayList<>();
        try {
            File directory = new File("src/main/resources/static");
            File[] files = directory.listFiles();

            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jks")) {
                    KeyStore keyStore = KeyStore.getInstance("JKS");
                    FileInputStream fileInputStream = new FileInputStream(file);
                    keyStore.load(fileInputStream, "password".toCharArray());

                    Enumeration<String> aliases = keyStore.aliases();
                    while (aliases.hasMoreElements()) {
                        String alias = aliases.nextElement();
                        Certificate certificate = keyStore.getCertificate(alias);

                        if (certificate instanceof X509Certificate) {
                            certificates.add(certificate);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Greška pri učitavanju sertifikata.");
        }
        return certificates;
    }

    private List<Certificate> getCertificatesByAliasesFromKeyStores(List<String> aliases) {
        List<Certificate> certificates = new ArrayList<>();
        try {
            File directory = new File("src/main/resources/static");
            File[] files = directory.listFiles();

            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jks")) {
                    KeyStore keyStore = KeyStore.getInstance("JKS");
                    FileInputStream fileInputStream = new FileInputStream(file);
                    keyStore.load(fileInputStream, "password".toCharArray());

                    Enumeration<String> aliases2 = keyStore.aliases();
                    while (aliases2.hasMoreElements()) {
                        String alias = aliases2.nextElement();
                        Certificate certificate = keyStore.getCertificate(alias);

                        if (certificate instanceof X509Certificate && aliases.contains(alias)) {
                            certificates.add(certificate);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Greška pri učitavanju sertifikata.");
        }
        return certificates;
    }

    private List<CertificateDto> mapCertificatesToDtos(List<Certificate> certificates, List<String> aliases) {
        List<CertificateDto> dtos = new ArrayList<>();
        int i = 0;
        for (Certificate certificate : certificates) {
            if (certificate instanceof X509Certificate) {
                X509Certificate x509Certificate = (X509Certificate) certificate;

                String type = x509Certificate.getBasicConstraints() == 0 ? "FALSE" : "TRUE";
                String alias = aliases == null ? null : aliases.get(i);
                CertificateDto dto = new CertificateDto(
                        x509Certificate.getSubjectDN().getName(),
                        x509Certificate.getIssuerDN().getName(),
                        x509Certificate.getNotBefore().toString(),
                        x509Certificate.getNotAfter().toString(),
                        type,
                        alias
                );
                dtos.add(dto);
                i++;
            }
        }
        return dtos;
    }
}
