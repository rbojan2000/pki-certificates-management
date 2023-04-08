package pki.certificates.management.controller;

import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pki.certificates.management.dto.CertificateDTO;
import pki.certificates.management.dto.CreateCertificateDTO;
import pki.certificates.management.service.implementations.CertificateService;

import java.io.IOException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificateController {

    @Autowired
    CertificateService certificateService;

    @PostMapping(path = "create")
    public ResponseEntity<Void> createEndEntityOrIntermediateCertificate(@RequestBody CreateCertificateDTO createCertificateDTO) throws UnrecoverableKeyException, CertificateException, IOException, OperatorCreationException, ParseException {

        certificateService.createEndEntityOrIntermediateCertificate(createCertificateDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<CertificateDTO> getAllCertificates() {
        return certificateService.getAllCertificates();
    }
}
