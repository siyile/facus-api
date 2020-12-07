package top.siyile.facusapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import top.siyile.facusapi.model.Session;

import java.util.List;

@RepositoryRestResource
public interface SessionRepository extends MongoRepository<Session, String>{
    List<Session> findByTagIgnoreCase(String tag);

    List<Session> findByStartTimeGreaterThanEqualAndEndTimeLessThanEqual(long startTime, long endTime);
    List<Session> findByStartTimeGreaterThanEqual(long startTime);
    List<Session> findByEndTimeLessThanEqual(long endTime);

    List<Session> findByStatusOrderByStartTime(String status);
    List<Session> findByDurationBetween(int minDuration, int maxDuration);
    List<Session> findByFirstAttendantOrSecondAttendant(String firstAttendant, String secondAttendant);
    List<Session> findByFirstAttendantOrSecondAttendantAndStatus(String firstAttendant,
                                                                 String secondAttendant,
                                                                 String status);
}
