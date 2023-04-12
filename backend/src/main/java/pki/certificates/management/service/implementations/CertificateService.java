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
import pki.certificates.management.keystore.ConfigurationManager;
import pki.certificates.management.keystore.KeyStoreReader;
import pki.certificates.management.keystore.KeyStoreWriter;
import pki.certificates.management.model.Issuer;
import pki.certificates.management.model.Subject;
import pki.certificates.management.model.User;

import pki.certificates.management.model.UserCertificate;
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

        Issuer issuer;

        issuer = keyStoreReader.readIssuerFromStore(configurationManager.getRootKeystorePath(), createCertificateDTO.aliasIssuer, configurationManager.getRootKeystorePassword().toCharArray(), configurationManager.getRootKeystorePassword().toCharArray());

        if(issuer == null) {
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
        List<CertificateDTO> certs =  getCertificatesByAliases(user.getCerts().stream()
                .map(UserCertificate::getAlias)
                .collect(Collectors.toList()));
        return certs;
    }

    public void revokeCertificate(String alias) {
        User user = userService.findByCertsAlias(alias);
        user.getCerts().stream()
                .filter(userCertificateDto -> userCertificateDto.getAlias().equals(alias))
                .findFirst()
                .ifPresent(userCertificateDto -> userCertificateDto.setRevoked(true));

        userService.updateUser(user);
    }

    private List<Certificate> getAllCertificatesFromKeyStores() {
        List<Certificate> rootCertificates = keyStoreReader.getAllCertificatesFromKeyStore(configurationManager.getRootKeystorePath(), configurationManager.getRootKeystorePassword());
        List<Certificate> otherCertificates = keyStoreReader.getAllCertificatesFromKeyStore(configurationManager.getOtherKeystorePath(), configurationManager.getOtherKeystorePassword());

        List<Certificate> allCertificates = new ArrayList<>(rootCertificates);
        allCertificates.addAll(otherCertificates);

        return allCertificates;
    }

    private static List<UserCertificate> getNonRevokedCerts(List<User> users) {
        return users.stream()
                .flatMap(user -> user.getCerts().stream())
                .filter(cert -> !cert.isRevoked())
                .map(cert -> new UserCertificate(cert.getAlias(), cert.isRevoked()))
                .collect(Collectors.toList());
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
