package pki.certificates.management.service.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pki.certificates.management.model.User;
import pki.certificates.management.model.UserCertificate;
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
        List<UserCertificate> certs = user.getCerts();
        certs.add(new UserCertificate(alias, false));
        user.setCerts(certs);
        userRepository.save(user);
    }

    @Override
    public User findByCertsAlias(String alias) {
        return userRepository.findByCertsAlias(alias);
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public boolean isRevoked(String alias) {
        User user = userRepository.findByAlias(alias);
        if (user == null) {
            // Alias nije pronađen
            return false;
        }
        for (UserCertificate certificate : user.getCerts()) {
            if (certificate.getAlias().equals(alias)) {
                // Pronađen je sertifikat sa prosleđenim aliasom, vraćamo njegovu isRevoked vrednost
                return certificate.isRevoked();
            }
        }
        // Alias nije pronađen u sertifikatima
        return false;
    }
}