package pki.certificates.management.service.interfaces;

import pki.certificates.management.dto.CertificateDTO;
import pki.certificates.management.model.User;

import java.util.List;

public interface IUserService {
    public List<CertificateDTO> userCertificates(String userID);

    public User createUser(User user);

}