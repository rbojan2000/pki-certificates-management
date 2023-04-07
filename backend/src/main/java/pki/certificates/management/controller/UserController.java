package pki.certificates.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pki.certificates.management.dto.CertificateDto;
import pki.certificates.management.model.User;
import pki.certificates.management.service.implementations.UserService;

import java.util.List;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(path = "/create")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        userService.createUser(user);
        return ResponseEntity.ok("Korisnik je uspešno dodat!");
    }

    @GetMapping(path = "/getUserCertificates")
    public List<CertificateDto> getUserCertificates() {



        return userService.userCertificates("6430097b79ec27b51b9e256d");
    }


}
