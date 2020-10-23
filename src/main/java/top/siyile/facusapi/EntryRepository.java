package top.siyile.facusapi;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface EntryRepository extends MongoRepository<Entry, String> {
    public Entry findByKey(String key);
    public List<Entry> findByValue(String value);
}
