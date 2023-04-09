package pki.certificates.management.service.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pki.certificates.management.model.User;
import pki.certificates.management.repository.UserRepository;
import pki.certificates.management.service.interfaces.IUserService;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserByID(String userID) {
        return userRepository.findById(userID).get();
    }

    @Override
    public void assignCertificateToUser(String alias, String userID) {
        User user = userRepository.findById(userID).get();
        List<String> certs = user.getCerts();
        certs.add(alias);
        user.setCerts(certs);
        userRepository.save(user);
    }
}
