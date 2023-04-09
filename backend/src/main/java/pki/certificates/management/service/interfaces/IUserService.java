package pki.certificates.management.service.interfaces;

import pki.certificates.management.dto.CertificateDTO;
import pki.certificates.management.model.User;

import java.util.List;

public interface IUserService {

    public User createUser(User user);

    public User getUserByID(String userID);

    void assignCertificateToUser(String alias, String userID);
}