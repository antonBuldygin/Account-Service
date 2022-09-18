package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Optional;

@Component
public class AuthenticationFailureListener implements
        ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    LogRepository logRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserDetailsServiceImpl userService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        System.out.println("bad");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date = LocalDateTime.now().format(dateTimeFormatter);
        Enumeration en1 = request.getHeaderNames();

        String code1 = new String();

        while (en1.hasMoreElements()) {
            //get header name Accept,Accept-Charset,Authorization,Connection,Host etc.
            String headerName = (String) en1.nextElement(); //nextElement() returns Object need type cast
            //get the value of the headerName
            String headerValue = request.getHeader(headerName);
            if (headerValue.contains("Basic")) {
                code1 = headerValue; break;
            }

        }

        String email = new String();
        if (code1.length() > 0) {
            code1 = code1.substring(6, code1.length()).trim();

            byte[] decodedBytes = Base64.getDecoder().decode(code1);
            String decodedString = new String(decodedBytes);
            String[] ar = decodedString.split(":");

            email = decodedString.equals(":") ? "null" : ar[0] == "" ? "null" : ar[0];
//            logRepository.save(new Log(date, "LOGIN_FAILED", email, request.getRequestURI(), request.getRequestURI()));
        } else {
            email = "null";
        }

        Optional<User> emailInRepo = userRepository.findByEmail(email.toLowerCase()).stream().findFirst();
        User user = null;


        if (emailInRepo.isPresent()) {
            user = emailInRepo.get();
        }


//        if (user != null) {
//            if (user.isAccountNonLocked()) {
//                if (userService.getUser().getFailedAttempt() < UserDetailsServiceImpl.MAX_FAILED_ATTEMPTS - 1) {
//                    userService.increaseFailedAttempts(); logRepository.save(new Log(date, "LOGIN_FAILED", email, request.getRequestURI(), request.getRequestURI()));
//                } else {
//                    userService.lock(); logRepository.save(new Log(date, "LOCK_USER", email, request.getRequestURI(), request.getRequestURI()));
//                }
//            }
//
//        }


    }
}
