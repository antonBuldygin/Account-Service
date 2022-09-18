package account;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;


@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    LogRepository logRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserDetailsServiceImpl userService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
        String date = LocalDateTime.now().format(dateTimeFormatter);
        Enumeration en = request.getHeaderNames();

        String code = new String();

        while (en.hasMoreElements()) {
            //get header name Accept,Accept-Charset,Authorization,Connection,Host etc.
            String headerName = (String) en.nextElement(); //nextElement() returns Object need type cast
            //get the value of the headerName
            String headerValue = request.getHeader(headerName);
            if (headerValue.contains("Basic")) {
                code = headerValue;
                break;
            }

        }
        String email = new String();
        if (code.length() > 0) {
            code = code.substring(6, code.length()).trim();

            byte[] decodedBytes = Base64.getDecoder().decode(code);
            String decodedString = new String(decodedBytes);
            String[] ar = decodedString.split(":");

            email = decodedString.equals(":") ? "null" : ar[0] == "" ? "null" : ar[0];
//            logRepository.save(new Log(date, "LOGIN_FAILED", email, request.getRequestURI(), request.getRequestURI()));
        } else {
            email = "null";
        }

        Optional<User> emailInRepo = userRepository.findByEmail(email.toLowerCase()).stream().findFirst();
        User user = null;

        List<Group> adminPresent = new ArrayList<>();

        if (emailInRepo.isPresent()) {
            user = emailInRepo.get();
            Set<Group> list = emailInRepo.get().getRolesToStore();
             adminPresent = list.stream().filter(x -> x.getRole().equals("ROLE_ADMINISTRATOR"))
                    .collect(Collectors.toList());
        }
        if (!emailInRepo.isPresent() && !email.equals("null")) {
            logRepository.save(new Log(date, "LOGIN_FAILED", email, request.getRequestURI(), request.getRequestURI()));
        }

        if (user != null) {
            if (user.isAccountNonLocked()) {
                if (userService.getUser().getFailedAttempt() <= UserDetailsServiceImpl.MAX_FAILED_ATTEMPTS) {
                    userService.increaseFailedAttempts();
                    logRepository.save(new Log(date, "LOGIN_FAILED", email, request.getRequestURI(), request.getRequestURI()));
                }
                if (userService.getUser().getFailedAttempt() == UserDetailsServiceImpl.MAX_FAILED_ATTEMPTS) {
                    logRepository.save(new Log(date, "BRUTE_FORCE", email, request.getRequestURI(), request.getRequestURI()));
                }
                if (userService.getUser().getFailedAttempt()
                        == UserDetailsServiceImpl.MAX_FAILED_ATTEMPTS&&adminPresent.isEmpty()) {
                    userService.lock();
                    logRepository.save(new Log(date, "LOCK_USER", email, "Lock user " + email, request.getRequestURI()));
                }
            }

        }


        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());

    }
}

