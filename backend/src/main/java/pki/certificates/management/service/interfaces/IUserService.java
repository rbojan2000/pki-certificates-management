package pki.certificates.management.service.interfaces;

import pki.certificates.management.model.User;

public interface IUserService {

    public User createUser(User user);

    public User getUserByID(String userID);

    void assignCertificateToUser(String alias, String userID);
}