package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    @Autowired
    UserRepository userRepository;

    User user;

    public User getUser() {
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can't remove ADMINISTRATOR role!");
        }
        if (username.equals("")) {throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Can't remove ADMINISTRATOR role!");
    }


        String emailLower = username.toLowerCase();

        Optional<User> check = userRepository.findByEmail(emailLower);
        if (check.isPresent()) {
            user = check.get();
        } else {
            throw new UsernameNotFoundException("Not found: " + username);
        }

        return new UserDetailsImpl(user);
    }

    public void increaseFailedAttempts() {
        int newFailAttempts = user.getFailedAttempt() + 1;
        user.setFailedAttempt(newFailAttempts);
       userRepository.save(user);
    }

    public void resetFailedAttempts() {

        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    public void lock() {
        user.setAccountNonLocked(false);

        userRepository.save(user);
    }

}
