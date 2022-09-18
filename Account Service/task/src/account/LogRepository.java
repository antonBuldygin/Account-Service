package account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface LogRepository  extends CrudRepository<Log, Long> {

        Iterable<Log> findAll();

}
