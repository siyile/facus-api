package top.siyile.facusapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import top.siyile.facusapi.model.User;

@RepositoryRestResource
public interface UserRepository extends MongoRepository<User, String>{

    public User findByEmail(String email);

}
