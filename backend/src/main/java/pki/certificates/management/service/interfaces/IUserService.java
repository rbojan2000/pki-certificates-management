package pki.certificates.management.service.interfaces;

import pki.certificates.management.dto.CertificateDto;
import pki.certificates.management.model.User;

import java.util.List;

public interface IUserService {
    public List<CertificateDto> userCertificates(String userID);


    public User createUser(User user);

}