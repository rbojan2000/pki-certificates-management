package pki.certificates.management.controller;
import org.bouncycastle.cert.ocsp.*;
import org.bouncycastle.cert.ocsp.jcajce.JcaCertificateID;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pki.certificates.management.dto.CertificateDto;
import pki.certificates.management.keystore.KeyStoreReader;
import pki.certificates.management.service.implementations.CertificateService;
import java.security.*;
import java.security.cert.CRLReason;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

import java.security.cert.X509Certificate;
import java.util.Date;
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
