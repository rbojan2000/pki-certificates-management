package pki.certificates.management.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pki.certificates.management.model.User;
import pki.certificates.management.model.UserCertificate;

public interface UserRepository extends MongoRepository<User, String> {
    User findByCertsAlias(String alias);

    @Query("{'certs.alias': ?0}")
    User findByAlias(String alias);
}
