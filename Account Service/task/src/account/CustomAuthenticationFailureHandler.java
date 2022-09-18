//package account;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Map;
//
//public class CustomAuthenticationFailureHandler
//        implements AuthenticationFailureHandler {
//
//    private ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public void onAuthenticationFailure(
//            HttpServletRequest request,
//            HttpServletResponse httpServletResponse,
//            AuthenticationException exception)
//            throws IOException, ServletException {
//
//        Map<String,Object> response = new HashMap<>();
//        response.put("status",403);
//        response.put("message","unauthorized api access");
////        response.put("time", LocalDateTime.now());
//
//        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
////        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        OutputStream out = httpServletResponse.getOutputStream();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.writerWithDefaultPrettyPrinter().writeValue(out,response);
//        //mapper.writeValue(out, response);
//
//        out.flush();
//    }
//}
