package account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface PaymentsRepository extends CrudRepository<Payments, Long> {
    Optional<Payments> findByEmployeeAndAndPeriod(String employee, String period);

    List<Payments> findByEmployee(String employee);

    public Payments save(Payments user);


    Optional<Payments> findById(Long id);

    void saveAndFlush(Payments u);


}
