//package account;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.LockedException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Optional;
//
//@Component
//public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
//
//    @Autowired
//    private UserDetailsServiceImpl userService;
//
//    @Override
//    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
//                                        AuthenticationException exception) throws IOException, ServletException {
//        String email = request.getParameter("email");
//        UserDetails user = userService.loadUserByUsername(email);
//
//        if (user != null) {
//            if (user.isAccountNonLocked()) {
//                if (userService.getUser().getFailedAttempt() < UserDetailsServiceImpl.MAX_FAILED_ATTEMPTS - 1) {
//                    userService.increaseFailedAttempts();
//                } else {
//                    userService.lock();
//                    exception = new LockedException("Your account has been locked due to 3 failed attempts."
//                            + " It will be unlocked after 24 hours.");
//                }
//            }
//
//        }
//
//
//        super.onAuthenticationFailure(request, response, exception);
//    }
//
//}