package pki.certificates.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pki.certificates.management.model.User;
import pki.certificates.management.repository.UserRepository;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    public UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> createUser(@RequestParam("name") String name, @RequestParam("email") String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);
        return ResponseEntity.ok("Korisnik " + name + " sa email adresom " + email + " je uspešno dodat!");
    }
}
