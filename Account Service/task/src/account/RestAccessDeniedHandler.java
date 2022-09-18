//package account;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//
//public class RestAccessDeniedHandler implements AccessDeniedHandler {
//
//    @Override
//    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException, IOException {
//
//        Map<String,Object> response = new HashMap<>();
//        response.put("status",httpServletResponse.getStatus());
//        response.put("message","unauthorized api access");
//        response.put("time", LocalDateTime.now());
//
//        //httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        OutputStream out = httpServletResponse.getOutputStream();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.writerWithDefaultPrettyPrinter().writeValue(out,response);
//        //mapper.writeValue(out, response);
//
//        out.flush();
//    }
//}
