package pki.certificates.management.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pki.certificates.management.model.User;

public interface UserRepository extends MongoRepository<User, String> {

}
