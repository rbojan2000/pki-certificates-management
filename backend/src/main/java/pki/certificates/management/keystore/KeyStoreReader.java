package pki.certificates.management.keystore;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.stereotype.Component;
import pki.certificates.management.model.Issuer;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class KeyStoreReader {
    //KeyStore je Java klasa za citanje specijalizovanih datoteka koje se koriste za cuvanje kljuceva
    //Tri tipa entiteta koji se obicno nalaze u ovakvim datotekama su:
    // - Sertifikati koji ukljucuju javni kljuc
    // - Privatni kljucevi
    // - Tajni kljucevi, koji se koriste u simetricnima siframa
    private KeyStore keyStore;

    public KeyStoreReader() {
        try {
            keyStore = KeyStore.getInstance("JKS", "SUN");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zadatak ove funkcije jeste da ucita podatke o izdavaocu i odgovarajuci privatni kljuc.
     * Ovi podaci se mogu iskoristiti da se novi sertifikati izdaju.
     *
     * @param keyStoreFile - datoteka odakle se citaju podaci
     * @param alias - alias putem kog se identifikuje sertifikat izdavaoca
     * @param password - lozinka koja je neophodna da se otvori key store
     * @param keyPass - lozinka koja je neophodna da se izvuce privatni kljuc
     * @return - podatke o izdavaocu i odgovarajuci privatni kljuc
     */
    public Issuer readIssuerFromStore(String keyStoreFile, String alias, char[] password, char[] keyPass) {
        try {
            // Load the keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream fileInputStream = new FileInputStream(keyStoreFile);
            keyStore.load(fileInputStream, password);

            // Get the certificate with the given alias
            Certificate cert = keyStore.getCertificate(alias);

            // Convert the certificate to X509Certificate
            X509Certificate x509Cert = (X509Certificate) cert;

            // Get the issuer name from the certificate
            X500Name issuerName = new JcaX509CertificateHolder(x509Cert).getIssuer();

            // Get the private key corresponding to the public key in the certificate
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPass);

            // Return an Issuer object that contains the private key, public key, and issuer name obtained from the certificate
            return new Issuer(privateKey, x509Cert.getPublicKey(), issuerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Ucitava sertifikat is KS fajla
     */
    public Certificate readCertificate(String keyStoreFile, String keyStorePass, String alias) {
        try {
            //kreiramo instancu KeyStore
            KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            //ucitavamo podatke
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(keyStoreFile));
            ks.load(in, keyStorePass.toCharArray());

            if(ks.isKeyEntry(alias)) {
                Certificate cert = ks.getCertificate(alias);
                return cert;
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Certificate getCertificateByAliasFromKeyStore(String alias) {
        try{
            File directory = new File("src/main/resources/static");
            File[] files = directory.listFiles();

            for (File file : files) {
                if(file.isFile() && file.getName().endsWith(".jks")) {
                    KeyStore keyStore = KeyStore.getInstance("JKS");
                    FileInputStream fileInputStream = new FileInputStream(file);
                    keyStore.load(fileInputStream, "password".toCharArray());

                    Enumeration<String> aliases2 = keyStore.aliases();

                    for (String al : Collections.list(aliases2)) {

                        Certificate certificate = keyStore.getCertificate(al);

                        if (certificate instanceof X509Certificate && alias.equals(al)) {
                            return certificate;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Ucitava privatni kljuc is KS fajla
     */
    public PrivateKey readPrivateKey(String keyStoreFile, String keyStorePass, String alias, String pass) {
        try {
            //kreiramo instancu KeyStore
            KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            //ucitavamo podatke
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(keyStoreFile));
            ks.load(in, keyStorePass.toCharArray());

            if(ks.isKeyEntry(alias)) {
                PrivateKey pk = (PrivateKey) ks.getKey(alias, pass.toCharArray());
                return pk;
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Ucitava sve sertifikate is KS fajla
     */
    public List<Certificate> getAllCertificatesFromKeyStore(String keyStoreFile, String keyStorePass) {
        List<Certificate> certificates = new ArrayList<>();

        try {
            KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(keyStoreFile));
            ks.load(in, keyStorePass.toCharArray());

            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate cert = ks.getCertificate(alias);
                certificates.add(cert);
            }
        } catch (KeyStoreException | NoSuchProviderException |
                 NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }

        return certificates;
    }

    /**
     * Ucitava sve sertifikate iz KS fajla po alijasima
     */
    public List<Certificate> getCertificatesFromKeyStoreByAliases(String keyStoreFile, String keyStorePass, List<String> aliases) {
        List<Certificate> certificates = new ArrayList<>();

        try {
            KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(keyStoreFile));
            ks.load(in, keyStorePass.toCharArray());

            for (String alias : aliases) {
                Certificate cert = ks.getCertificate(alias);
                if (cert != null) {
                    certificates.add(cert);
                }
            }
        } catch (KeyStoreException | NoSuchProviderException |
                 NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }

        return certificates;
    }

    /**
     * Ucitava sve alijase iz KS fajla
     */
    public List<String> getAllAliasesFromKeyStore(String keyStoreFile, String keyStorePass) {
        List<String> aliases = new ArrayList<>();

        try {
            KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(keyStoreFile));
            ks.load(in, keyStorePass.toCharArray());

            Enumeration<String> e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = e.nextElement();
                aliases.add(alias);
            }
        } catch (KeyStoreException | NoSuchProviderException |
                 NoSuchAlgorithmException | CertificateException | IOException ex) {
            ex.printStackTrace();
        }

        return aliases;
    }
}