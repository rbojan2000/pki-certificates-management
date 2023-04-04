package pki.certificates.management.controller;


import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pki.certificates.management.keystore.KeyStoreWriter;

import java.security.*;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    @PostMapping(path = "create")
    public ResponseEntity<?> createRootCertificate() throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, CertificateException {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Generiranje KeyPair-a
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Postavljanje podataka o vlasniku certifikata
        X500Name owner = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "My Root CA")
                .addRDN(BCStyle.O, "My Organization")
                .addRDN(BCStyle.OU, "My Unit")
                .addRDN(BCStyle.L, "My City")
                .addRDN(BCStyle.ST, "My State")
                .addRDN(BCStyle.C, "US")
                .build();

        // Postavljanje seriskog broja certifikata
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

        // Postavljanje roka važenja certifikata
        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // Jedan dan unazad
        Date notAfter = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000); // Jedna godina unaprijed

        // Kreiranje objekta JcaX509v3CertificateBuilder za generiranje self-signed sertifikata
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                owner,
                serialNumber,
                notBefore,
                notAfter,
                owner,
                keyPair.getPublic());

        // Kreiranje ContentSigner-a za potpisivanje certifikata
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());

        // Generiranje self-signed sertifikata
        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);

        // Ispisivanje generiranog certifikata
        System.out.println(cert.toString());

        KeyStoreWriter keyStoreWriter = new KeyStoreWriter();


        keyStoreWriter.loadKeyStore("src/main/resources/static/root-keystore.jks",  "password".toCharArray());

        // Upisujemo sertifikat u keystore
        String alias = "my Root CA";
        PrivateKey privateKey = keyPair.getPrivate();
        keyStoreWriter.write(alias, privateKey, "password".toCharArray(), cert);

// Čuvamo keystore fajl
        keyStoreWriter.saveKeyStore("src/main/resources/static/root-keystore.jks",  "password".toCharArray());

        System.out.println("Self-signed sertifikat je uspješno sačuvan u JKS fajl.");
        return new ResponseEntity(HttpStatus.OK);
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
