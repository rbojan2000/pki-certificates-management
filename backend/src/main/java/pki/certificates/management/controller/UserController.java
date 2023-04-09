package pki.certificates.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pki.certificates.management.model.User;
import pki.certificates.management.service.implementations.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping(path = "/getUser/{userID}")
    public User getUserByID(@PathVariable("userID") String userID) {
        return userService.getUserByID(userID);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        userService.createUser(user);
        return ResponseEntity.ok("Korisnik je uspešno dodat!");
    }
}
