package pki.certificates.management.controller;


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pki.certificates.management.dto.CertificateDto;
import pki.certificates.management.keystore.KeyStoreReader;
import pki.certificates.management.keystore.KeyStoreWriter;
import pki.certificates.management.model.Issuer;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificateController {

    @PostMapping(path = "create")
    public ResponseEntity<?> createRootCertificate() throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, CertificateException {
        // Generiranje KeyPair-a
        // Učitavanje privatnog ključa root sertifikata
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        Issuer issuer = keyStoreReader.readIssuerFromStore("src/main/resources/static/root-keystore.jks", "my root CA", "password".toCharArray(), "password".toCharArray());

        java.security.cert.Certificate loadedCertificate = keyStoreReader.readCertificate("src/main/resources/static/root-keystore.jks", "password", "my root CA");

        X509Certificate rootCert = (X509Certificate) loadedCertificate;

        // Generiranje KeyPair-a
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Postavljanje podataka o vlasniku certifikata
        X500Name owner = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "My Intermediate CA")
                .addRDN(BCStyle.O, "My Organization")
                .addRDN(BCStyle.OU, "My Unit")
                .addRDN(BCStyle.L, "Novi Sad")
                .addRDN(BCStyle.ST, "Srbija")
                .addRDN(BCStyle.C, "RS")
                .build();

        // Postavljanje seriskog broja certifikata
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

        // Postavljanje roka važenja certifikata
        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // Jedan dan unazad
        Date notAfter = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000); // Jedna godina unaprijed

        // Kreiranje objekta JcaX509v3CertificateBuilder za generiranje intermediate sertifikata
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                rootCert,
                serialNumber,
                notBefore,
                notAfter,
                owner,
                keyPair.getPublic());

        // Kreiranje ContentSigner-a za potpisivanje certifikata
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(issuer.getPrivateKey());

        // Generiranje intermediate sertifikata
        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);

        // Ispisivanje generiranog certifikata
        System.out.println(cert.toString());

        KeyStoreWriter keyStoreWriter = new KeyStoreWriter();

        // Inicijalizacija fajla za cuvanje sertifikata
        System.out.println("Cuvanje certifikata u jks fajl:");
        keyStoreWriter.loadKeyStore("src/main/resources/static/other-keystore.jks",  "password".toCharArray());
        PrivateKey pk = keyPair.getPrivate();
        keyStoreWriter.write("my intermediate CA", pk, "password".toCharArray(), cert);
        keyStoreWriter.saveKeyStore("src/main/resources/static/other-keystore.jks",  "password".toCharArray());

        return null;

    }

    @GetMapping
    public List<CertificateDto> getAllCertificates() {
        List<CertificateDto> returnCertifitcates = new ArrayList<>();
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
                            X509Certificate x509Certificate = (X509Certificate) certificate;

                            String type = "";
                            if(x509Certificate.getBasicConstraints() == 0){
                                type = "FALSE";
                            } else {
                                type = "TRUE";
                            }
                            CertificateDto certificateDto = new CertificateDto(x509Certificate.getSubjectDN().getName(),
                                    x509Certificate.getIssuerDN().getName(),
                                    x509Certificate.getNotBefore().toString(),
                                    x509Certificate.getNotAfter().toString(),
                                    type);
                            returnCertifitcates.add(certificateDto);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Greška pri učitavanju sertifikata.");
        }
        return returnCertifitcates;
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

}
