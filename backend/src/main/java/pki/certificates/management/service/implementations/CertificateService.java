package pki.certificates.management.service.implementations;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pki.certificates.management.dto.CertificateDTO;
import pki.certificates.management.dto.CreateCertificateDTO;
import pki.certificates.management.keystore.KeyStoreReader;
import pki.certificates.management.keystore.KeyStoreWriter;
import pki.certificates.management.model.Issuer;
import pki.certificates.management.model.Subject;
import pki.certificates.management.model.User;
import pki.certificates.management.service.interfaces.ICertificateService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CertificateService implements ICertificateService {

    @Autowired
    KeyStoreReader keyStoreReader;

    @Autowired
    KeyStoreWriter keyStoreWriter;

    @Autowired
    UserService userService;


    @Override
    public List<CertificateDTO> getAllCertificates() {
        List<Certificate> certificates = getAllCertificatesFromKeyStores();
        return mapCertificatesToDtos(certificates, null);
    }

    @Override
    public List<CertificateDTO> getCertificatesByAliases(List<String> aliases) {
        List<Certificate> certificates = getCertificatesByAliasesFromKeyStores(aliases);
        return mapCertificatesToDtos(certificates, aliases);
    }

    @Override
    public X509Certificate createEndEntityOrIntermediateCertificate(CreateCertificateDTO createCertificateDTO) throws  IOException, CertificateException, OperatorCreationException, ParseException {

        // Generate subject
        Subject subject = generateSubject(createCertificateDTO.subjectCN, createCertificateDTO.subjectO,
                createCertificateDTO.subjectOU, createCertificateDTO.subjectUN, createCertificateDTO.subjectCountry);

        Issuer issuer = new Issuer();

        try {
            issuer = keyStoreReader.readIssuerFromStore("src/main/resources/static/root-keystore.jks",
                    createCertificateDTO.aliasIssuer, "password".toCharArray(), "password".toCharArray());
        } catch (Exception e) {
            issuer = keyStoreReader.readIssuerFromStore("src/main/resources/static/other-keystore.jks",
                    createCertificateDTO.aliasIssuer, "password".toCharArray(), "password".toCharArray());

        }

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC")
                .build(issuer.getPrivateKey());

        // Create certificate generator
        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(issuer.getX500Name(),
                new BigInteger(128, new SecureRandom()),
                parseDate(createCertificateDTO.startDate),
                parseDate(createCertificateDTO.endDate),
                subject.getX500Name(),
                subject.getPublicKey()
        )
            .addExtension(Extension.basicConstraints, Boolean.valueOf(createCertificateDTO.selectedAuthority), new BasicConstraints(Boolean.valueOf(createCertificateDTO.selectedAuthority)))
            .addExtension(Extension.keyUsage, Boolean.valueOf(createCertificateDTO.selectedAuthority), new KeyUsage(KeyUsage.keyCertSign));

        // Generate certificate holder
        X509CertificateHolder certHolder = certGen.build(contentSigner);

        // Convert certificate holder to X509Certificate
        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter().setProvider("BC");
        X509Certificate certificate = certConverter.getCertificate(certHolder);

        // Store certificate in key store
        keyStoreWriter.loadKeyStore("src/main/resources/static/other-keystore.jks", "password".toCharArray());
        String alias = generateAlias(createCertificateDTO.subjectCN);
        keyStoreWriter.write(alias, issuer.getPrivateKey(), "password".toCharArray(), certificate);
        keyStoreWriter.saveKeyStore("src/main/resources/static/other-keystore.jks", "password".toCharArray());

        userService.assignCertificateToUser(alias, createCertificateDTO.userID);

        return certificate;
    }

    @Override
    public X509Certificate createRootCertificate(CreateCertificateDTO createCertificateDTO) throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, ParseException, CertIOException, CertificateException {
        // Generate subject
        Subject subject = generateSubject(createCertificateDTO.subjectCN, createCertificateDTO.subjectO,
                createCertificateDTO.subjectOU, createCertificateDTO.subjectUN, createCertificateDTO.subjectCountry);

        // Generate key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Create content signer
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC")
                .build(keyPair.getPrivate());

        // Create certificate generator
        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                subject.getX500Name(),
                new BigInteger(128, new SecureRandom()),
                parseDate(createCertificateDTO.startDate),
                parseDate(createCertificateDTO.endDate),
                subject.getX500Name(),
                keyPair.getPublic()
        )
            .addExtension(Extension.basicConstraints, true, new BasicConstraints(true))
            .addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign));

        // Generate certificate holder
        X509CertificateHolder certHolder = certGen.build(contentSigner);

        // Convert certificate holder to X509Certificate
        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter().setProvider("BC");
        X509Certificate certificate = certConverter.getCertificate(certHolder);

        String alias = generateAlias(createCertificateDTO.subjectCN);

        // Store certificate in key store
        keyStoreWriter.loadKeyStore("src/main/resources/static/root-keystore.jks", "password".toCharArray());
        keyStoreWriter.write(alias, keyPair.getPrivate(), "password".toCharArray(), certificate);
        keyStoreWriter.saveKeyStore("src/main/resources/static/root-keystore.jks", "password".toCharArray());

        userService.assignCertificateToUser(alias, createCertificateDTO.userID);


        return certificate;
    }

    @Override
    public List<CertificateDTO> userCertificates(String userID) {

        User user = userService.getUserByID(userID);
        return getCertificatesByAliases(user.getCerts());

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

                    for (String alias : Collections.list(aliases2)) {

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

    private List<CertificateDTO> mapCertificatesToDtos(List<Certificate> certificates, List<String> aliases) {
        List<CertificateDTO> dtos = new ArrayList<>();
        int i = 0;
        for (Certificate certificate : certificates) {
            if (certificate instanceof X509Certificate) {
                X509Certificate x509Certificate = (X509Certificate) certificate;

                String type = x509Certificate.getBasicConstraints() == 0 ? "FALSE" : "TRUE";
                String alias = aliases == null ? null : aliases.get(i);
                CertificateDTO dto = new CertificateDTO(
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

    public Subject generateSubject(String subjectCN, String subjectO, String subjectOU, String subjectUN, String subjectCountry) {
        KeyPair keyPairSubject = generateKeyPair();

        //klasa X500NameBuilder pravi X500Name objekat koji predstavlja podatke o vlasniku
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, subjectCN);
        builder.addRDN(BCStyle.O, subjectO);
        builder.addRDN(BCStyle.OU, subjectOU);
        builder.addRDN(BCStyle.C, subjectCountry);

        return new Subject(keyPairSubject.getPublic(), builder.build());
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



    private Date parseDate(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.parse(date);
    }

    public static String generateAlias(String companyName) {
        UUID uuid = UUID.randomUUID();
        return companyName.trim().replaceAll("\\s+", "").concat("-").concat(uuid.toString().replace("-", ""));
    }


}
