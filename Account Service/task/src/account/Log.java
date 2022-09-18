package account;

import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Component
@Entity
@Table(name = "log")
public class Log {

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE)
   @Column(name = "id", nullable = false)
   private Long id;

   @Column(name = "date")
   @NotNull
   @NotEmpty
   String  date;

   @Column(name = "action")
   @NotNull
   @NotEmpty
   String action;

   @Column(name = "subject")
   @NotNull
   @NotEmpty
   String subject;

   @Column(name = "object")
   @NotNull
   @NotEmpty
   String object;

   @Column(name = "path")
   @NotNull
   @NotEmpty
   String path;

   public Log(String date, String action, String subject, String object, String path) {
      this.date = date;
      this.action = action;
      this.subject = subject;
      this.object = object;
      this.path = path;
   }

   public Log() {
   }

   public String getDate() {
      return date;
   }

   public void setDate(String date) {
      this.date = date;
   }

   public String getAction() {
      return action;
   }

   public void setAction(String action) {
      this.action = action;
   }

   public String getSubject() {
      return subject;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }

   public String getObject() {
      return object;
   }

   public void setObject(String object) {
      this.object = object;
   }

   public String getPath() {
      return path;
   }

   public void setPath(String path) {
      this.path = path;
   }
}
