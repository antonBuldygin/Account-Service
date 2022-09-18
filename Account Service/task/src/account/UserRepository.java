package account;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Repository

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String username);


    public User save(User user);


    Optional<User> findById(Long id);

    void saveAndFlush(User u);

    @Transactional
    public List<User> deleteByEmail(String username);

    @Query("UPDATE User u SET u.failedAttempt = ?1 WHERE u.email = ?2")
    @Modifying
    public void updateFailedAttempts(int failAttempts, String email);
}
