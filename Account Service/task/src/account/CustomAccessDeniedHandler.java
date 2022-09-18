package account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Autowired
    LogRepository logRepository;

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                       AccessDeniedException e) throws IOException, ServletException {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
        String date = LocalDateTime.now().format(dateTimeFormatter);
        Enumeration en = httpServletRequest.getHeaderNames();

        String code = new String();

        while (en.hasMoreElements()) {
            //get header name Accept,Accept-Charset,Authorization,Connection,Host etc.
            String headerName = (String) en.nextElement(); //nextElement() returns Object need type cast
            //get the value of the headerName
            String headerValue = httpServletRequest.getHeader(headerName);
            if (headerValue.contains("Basic")) {
                code = headerValue;
            }

        }
        String email = new String();
        if(code.length()>0)
        {code = code.substring(6, code.length()).trim();

            byte[] decodedBytes = Base64.getDecoder().decode(code);
            String decodedString = new String(decodedBytes);
            String[] ar = decodedString.split(":");

            email =decodedString.equals(":")?"null": ar[0] ==""? "null":ar[0];
            logRepository.save(new Log(date, "ACCESS_DENIED", email, httpServletRequest.getRequestURI(), httpServletRequest.getRequestURI()));}
        else  {email= "null";}

        Map<String,Object> response = new HashMap<>();
        response.put("status", 403);
        response.put("error", "Forbidden");
        response.put("message","Access Denied!");
        response.put("time", Calendar.getInstance().getTime());
        response.put("path", httpServletRequest.getRequestURI());

        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        OutputStream out = httpServletResponse.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(out,response);
        //mapper.writeValue(out, response);

        out.flush();
    }
    }
