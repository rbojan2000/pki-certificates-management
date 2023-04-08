package pki.certificates.management.service.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pki.certificates.management.dto.CertificateDTO;
import pki.certificates.management.model.User;
import pki.certificates.management.repository.UserRepository;
import pki.certificates.management.service.interfaces.IUserService;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateService certificateService;

    @Override
    public List<CertificateDTO> userCertificates(String userID) {
        User user = userRepository.findById(userID).get();
        return  certificateService.getCertificatesByAliases(user.getCerts());

    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
