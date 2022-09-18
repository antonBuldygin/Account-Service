package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationSuccessEventListener implements
        ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserDetailsServiceImpl userService;


    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent e) {

        System.out.println("good");

        UserDetailsImpl userDetails =  (UserDetailsImpl) e.getAuthentication().getPrincipal();
        User user = userDetails.getUser();
        if (user.getFailedAttempt() > 0) {
            userService.resetFailedAttempts();
        }
    }
}