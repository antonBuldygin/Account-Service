//package account;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    @Autowired
//    private UserDetailsServiceImpl userService;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//        UserDetailsImpl userDetails =  (UserDetailsImpl) authentication.getPrincipal();
//        User user = userDetails.getUser();
//        if (user.getFailedAttempt() > 0) {
//            userService.resetFailedAttempts();
//        }
//
//        super.onAuthenticationSuccess(request, response, authentication);
//    }
//
//}