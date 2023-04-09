package pki.certificates.management.controller;


import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pki.certificates.management.dto.CertificateDto;
import pki.certificates.management.service.implementations.CertificateService;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificateController {

    @Autowired
    CertificateService certificateService;


    @PostMapping(path = "create")
    public ResponseEntity<?> createRootCertificate() throws NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException, CertificateException {
        return null;

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

    @GetMapping
    public List<CertificateDto> getAllCertificates() {
        return certificateService.getAllCertificates();
    }


    @GetMapping(path = "/revoke/{alias}")
    @CrossOrigin(origins = "http://localhost:4200")
    public void revokeCertificate(@PathVariable String alias) {
        certificateService.revokeCertificate(alias);
    }
}
