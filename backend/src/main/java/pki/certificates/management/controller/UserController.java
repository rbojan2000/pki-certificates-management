package pki.certificates.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pki.certificates.management.dto.CertificateDto;
import pki.certificates.management.service.implementations.UserService;

import java.util.List;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<String> createUser(@RequestParam("name") String name, @RequestParam("email") String email) {

        return ResponseEntity.ok("Korisnik " + name + " sa email adresom " + email + " je uspešno dodat!");
    }

    @GetMapping(path = "/getUserCertificates")
    public List<CertificateDto> getUserCertificates() {

        userService.userCertificates("642dafe9d9e299372bb8c612");

        return null;
    }


}
