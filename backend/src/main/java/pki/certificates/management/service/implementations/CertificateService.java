package pki.certificates.management.service.implementations;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.*;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pki.certificates.management.dto.CertificateDTO;
import pki.certificates.management.dto.CreateCertificateDTO;
import pki.certificates.management.keystore.ConfigurationManager;
import pki.certificates.management.keystore.KeyStoreReader;
import pki.certificates.management.keystore.KeyStoreWriter;
import pki.certificates.management.model.Issuer;
import pki.certificates.management.model.Subject;
import pki.certificates.management.model.User;

import pki.certificates.management.model.UserCertificate;
import pki.certificates.management.service.interfaces.ICertificateService;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateService implements ICertificateService {

    @Autowired
    KeyStoreReader keyStoreReader;

    @Autowired
    KeyStoreWriter keyStoreWriter;

    @Autowired
    ConfigurationManager configurationManager;

    @Autowired
    UserService userService;

    public static String generateAlias(String companyName) {
        UUID uuid = UUID.randomUUID();
        return companyName.trim().replaceAll("\\s+", "").concat("-").concat(uuid.toString().replace("-", ""));
    }

    @Override
    public List<CertificateDTO> getAllCertificates() {
        List<Certificate> certificates = getAllCertificatesFromKeyStores();
        List<String> aliases = getAliasesFromKeyStore();
        return mapCertificatesToDtos(certificates, aliases);
    }

    @Override
    public List<CertificateDTO> getCertificatesByAliases(List<String> aliases) {
        List<Certificate> certificates = getCertificatesByAliasesFromKeyStores(aliases);
        return mapCertificatesToDtos(certificates, aliases);
    }

    @Override
    public X509Certificate createEndEntityOrIntermediateCertificate(CreateCertificateDTO createCertificateDTO) throws IOException, CertificateException, OperatorCreationException, ParseException {

        // Generate subject
        Subject subject = generateSubject(createCertificateDTO.subjectCN, createCertificateDTO.subjectO, createCertificateDTO.subjectOU, createCertificateDTO.subjectUN, createCertificateDTO.subjectCountry);

        Issuer issuer = new Issuer();
        Properties props = new Properties();
        try {
            issuer = keyStoreReader.readIssuerFromStore(configurationManager.getRootKeystorePath(), createCertificateDTO.aliasIssuer, configurationManager.getRootKeystorePassword().toCharArray(), configurationManager.getRootKeystorePassword().toCharArray());
        } catch (Exception e) {
            issuer = keyStoreReader.readIssuerFromStore(configurationManager.getOtherKeystorePath(), createCertificateDTO.aliasIssuer, configurationManager.getOtherKeystorePassword().toCharArray(), configurationManager.getOtherKeystorePassword().toCharArray());

        }

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(issuer.getPrivateKey());

        // Create certificate generator
        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
                issuer.getX500Name(),
                new BigInteger(128, new SecureRandom()),
                parseDate(createCertificateDTO.startDate),
                parseDate(createCertificateDTO.endDate),
                subject.getX500Name(),
                subject.getPublicKey())
                .addExtension(Extension.basicConstraints, Boolean.valueOf(createCertificateDTO.selectedAuthority),
                        new BasicConstraints(Boolean.valueOf(createCertificateDTO.selectedAuthority)))
                .addExtension(Extension.keyUsage, Boolean.valueOf(createCertificateDTO.selectedAuthority),
                        new KeyUsage(KeyUsage.keyCertSign));

        // Generate certificate holder
        X509CertificateHolder certHolder = certGen.build(contentSigner);

        // Convert certificate holder to X509Certificate
        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter().setProvider("BC");
        X509Certificate certificate = certConverter.getCertificate(certHolder);

        // Store certificate in key store
        keyStoreWriter.loadKeyStore(configurationManager.getOtherKeystorePath(), configurationManager.getOtherKeystorePassword().toCharArray());
        String alias = generateAlias(createCertificateDTO.subjectCN);
        keyStoreWriter.write(alias, issuer.getPrivateKey(), "password".toCharArray(), certificate);
        keyStoreWriter.saveKeyStore(configurationManager.getOtherKeystorePath(), configurationManager.getOtherKeystorePassword().toCharArray());

        return certificate;
    }

    @Override
    public X509Certificate createRootCertificate(CreateCertificateDTO createCertificateDTO) throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, ParseException, CertIOException, CertificateException {
        // Generate subject
        Subject subject = generateSubject(createCertificateDTO.subjectCN, createCertificateDTO.subjectO, createCertificateDTO.subjectOU, createCertificateDTO.subjectUN, createCertificateDTO.subjectCountry);

        // Generate key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Create content signer
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(keyPair.getPrivate());

        // Create certificate generator
        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(subject.getX500Name(), new BigInteger(128, new SecureRandom()), parseDate(createCertificateDTO.startDate), parseDate(createCertificateDTO.endDate), subject.getX500Name(), keyPair.getPublic()).addExtension(Extension.basicConstraints, true, new BasicConstraints(true)).addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign));

        // Generate certificate holder
        X509CertificateHolder certHolder = certGen.build(contentSigner);

        // Convert certificate holder to X509Certificate
        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter().setProvider("BC");
        X509Certificate certificate = certConverter.getCertificate(certHolder);

        String alias = generateAlias(createCertificateDTO.subjectCN);

        // Store certificate in key store
        keyStoreWriter.loadKeyStore(configurationManager.getRootKeystorePath(), configurationManager.getRootKeystorePassword().toCharArray());
        keyStoreWriter.write(alias, keyPair.getPrivate(), configurationManager.getRootKeystorePassword().toCharArray(), certificate);
        keyStoreWriter.saveKeyStore(configurationManager.getRootKeystorePath(), configurationManager.getRootKeystorePassword().toCharArray());

        userService.assignCertificateToUser(alias, createCertificateDTO.userID);


        return certificate;
    }

    @Override
    public List<CertificateDTO> userCertificates(String userID) {
        User user = userService.getUserByID(userID);
        List<CertificateDTO> certs = getCertificatesByAliases(user.getCerts().stream()
                .map(UserCertificate::getAlias)
                .collect(Collectors.toList()));
        return certs;
    }

    public void revokeCertificate(String alias) {
        try {
            KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            BufferedInputStream in = new BufferedInputStream(new FileInputStream("src/main/resources/static/other-keystore.jks"));
            ks.load(in, "password".toCharArray());

            Certificate cert = ks.getCertificate(alias);
            X509Certificate revokedCert = (X509Certificate) cert;

            X509CRL crl = generateCRL(revokedCert, alias);

            // Čuvanje CRL-a u fajlu
            FileOutputStream crlOut = new FileOutputStream("src/main/resources/static/crl/crl.crl");
            crlOut.write(crl.getEncoded());
            crlOut.close();

        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException |
                 NoSuchProviderException | UnrecoverableKeyException | OperatorCreationException | CRLException ex) {
            throw new RuntimeException(ex);
        }

        User user = userService.findByCertsAlias(alias);
        user.getCerts().stream()
                .filter(userCertificateDto -> userCertificateDto.getAlias().equals(alias))
                .findFirst()
                .ifPresent(userCertificateDto -> userCertificateDto.setRevoked(true));

        userService.updateUser(user);
    }

    public void saveCertificateToFile(String alias) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS", "SUN");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream("src/main/resources/static/root-keystore.jks"));
        ks.load(in, "password".toCharArray());

        Certificate cert = ks.getCertificate(alias);
        X509Certificate downCert = (X509Certificate) cert;
        String directoryPath = "src/main/resources/static/saved";
        String fileName = alias + ".crt";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(downCert.getEncoded());
        fos.close();
    }

    private X509CRL generateCRL(X509Certificate revokedCert, String alias) throws NoSuchAlgorithmException, CertificateException, CRLException, IOException, KeyStoreException, NoSuchProviderException, UnrecoverableKeyException, OperatorCreationException {
        // Čitanje postojećeg CRL fajla (ako postoji)
        File crlFile = new File("src/main/resources/static/crl/crl.crl");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<X509CRLEntry> revokedCertificates = new ArrayList<>();
        if (crlFile.exists()) {
            FileInputStream fis = new FileInputStream(crlFile);
            X509CRL crl = (X509CRL) cf.generateCRL(fis);
            revokedCertificates.addAll(crl.getRevokedCertificates());
            fis.close();
        }

        // Kreiranje objekta X509v2CRLBuilder
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(new X500Name(revokedCert.getIssuerX500Principal().getName()), new Date());

        // Dodavanje povučenog sertifikata u CRL
        crlBuilder.addCRLEntry(revokedCert.getSerialNumber(), new Date(), CRLReason.keyCompromise);

        // Dodavanje postojećih povučenih sertifikata u CRL
        for (X509CRLEntry entry : revokedCertificates) {
            byte[] extensionValue = entry.getExtensionValue(Extension.basicConstraints.getId());
            if (extensionValue != null) {
                Extension extension = new Extension(Extension.basicConstraints, false, ASN1OctetString.getInstance(new GeneralNames(new GeneralName(new X500Name(Arrays.toString(extensionValue))))));
                Extensions extensions = new Extensions(extension);
                crlBuilder.addCRLEntry(entry.getSerialNumber(), entry.getRevocationDate(), extensions);
            } else {
                crlBuilder.addCRLEntry(entry.getSerialNumber(), entry.getRevocationDate(), null);
            }
        }

        // Potpisivanje CRL-a
        KeyStore ks = KeyStore.getInstance("JKS", "SUN");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream("src/main/resources/static/other-keystore.jks"));
        ks.load(in, "password".toCharArray());
        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "password".toCharArray());
        X509CRLHolder crlHolder = crlBuilder.build(new JcaContentSignerBuilder("SHA256withRSA").build(privateKey));
        JcaX509CRLConverter crlConverter = new JcaX509CRLConverter();
        X509CRL crl = crlConverter.getCRL(crlHolder);

        // Čuvanje CRL-a u fajlu
        FileOutputStream crlOut = new FileOutputStream("src/main/resources/static/crl/crl.crl");
        crlOut.write(crl.getEncoded());
        crlOut.close();

        return crl;
    }

    private List<Certificate> getAllCertificatesFromKeyStores() {
        List<Certificate> rootCertificates = keyStoreReader.getAllCertificatesFromKeyStore(configurationManager.getRootKeystorePath(), configurationManager.getRootKeystorePassword());
        List<Certificate> otherCertificates = keyStoreReader.getAllCertificatesFromKeyStore(configurationManager.getOtherKeystorePath(), configurationManager.getOtherKeystorePassword());

        List<Certificate> allCertificates = new ArrayList<>(rootCertificates);
        allCertificates.addAll(otherCertificates);

        return allCertificates;
    }

    private List<String> getAliasesFromKeyStore() {
        List<String> rootCertificatesAliases = keyStoreReader.getAllAliasesFromKeyStore(configurationManager.getRootKeystorePath(), configurationManager.getRootKeystorePassword());
        List<String> otherCertificatesAliases = keyStoreReader.getAllAliasesFromKeyStore(configurationManager.getOtherKeystorePath(), configurationManager.getOtherKeystorePassword());

        List<String> allCertificatesAliases = new ArrayList<>(rootCertificatesAliases);
        allCertificatesAliases.addAll(otherCertificatesAliases);

        return allCertificatesAliases;
    }

    private List<Certificate> getCertificatesByAliasesFromKeyStores(List<String> aliases) {
        List<Certificate> rootCertificates = keyStoreReader.getCertificatesFromKeyStoreByAliases(configurationManager.getRootKeystorePath(), configurationManager.getRootKeystorePassword(), aliases);
        List<Certificate> otherCertificates = keyStoreReader.getCertificatesFromKeyStoreByAliases(configurationManager.getOtherKeystorePath(), configurationManager.getOtherKeystorePassword(), aliases);

        List<Certificate> allCertificates = new ArrayList<>(rootCertificates);
        allCertificates.addAll(otherCertificates);

        return allCertificates;
    }

    private List<CertificateDTO> mapCertificatesToDtos(List<Certificate> certificates, List<String> aliases) {
        List<CertificateDTO> dtos = new ArrayList<>();
        int i = 0;
        for (Certificate certificate : certificates) {
            if (certificate instanceof X509Certificate x509Certificate) {

                String type = x509Certificate.getBasicConstraints() < 0 ? "FALSE" : "TRUE";
                String alias = aliases == null ? null : aliases.get(i);
                CertificateDTO dto = new CertificateDTO(x509Certificate.getSubjectDN().getName(), x509Certificate.getIssuerDN().getName(), x509Certificate.getNotBefore().toString(), x509Certificate.getNotAfter().toString(), type, alias, userService.isRevoked(alias));
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
}
