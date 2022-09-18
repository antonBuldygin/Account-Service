package account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface GroupRepository extends CrudRepository<Group, Long> {

        Optional<Group> findByRole(String name);



    }

