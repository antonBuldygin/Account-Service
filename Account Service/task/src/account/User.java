package account;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Component
@Entity
@Table(name = "savedUs")
public class User {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password")
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    private String password;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    @NotNull
    @NotEmpty
    private String name;

    @Column(name = "lastname")
    @NotNull
    @NotEmpty
    private String lastname;

    @Column(name = "email")
    @NotNull
    @NotEmpty
    private String email;

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> roles = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_group",
            joinColumns = @JoinColumn(name = "user_id",
                    referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "group_id",
                    referencedColumnName = "id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Group> rolesToStore = new HashSet<>();


    @Column(name = "account_non_locked")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean accountNonLocked ;

    @Column(name = "failed_attempt")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int failedAttempt;

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }



    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public int getFailedAttempt() {
        return failedAttempt;
    }

    public void setFailedAttempt(int failedAttempt) {
        this.failedAttempt = failedAttempt;
    }

    public List<String> setRoles() {
        rolesToStore.forEach(role -> roles.add((role.getRole())));
        Collections.sort(roles);
        return roles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Set<Group> getRolesToStore() {
        return rolesToStore;
    }

    public void addRole(Group role) {
        rolesToStore.add(role);
    }

    public void removeRole(Group role) {
        rolesToStore.remove(role);
    }

    public void setRolesToStore(Set<Group> roles) {
        this.rolesToStore = roles;
    }

//    private String roles = "ROLE_USER";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User() {
        this.setAccountNonLocked(true);
    }

    public User(String password, Long id, String name, String lastname,
                String email, Set<Group> role) {
        this.password = password;
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.rolesToStore = role;
        this.setAccountNonLocked(true);
    }


    public User(String password, Long id, String name, String lastname,
                String email, Set<Group> rolesToStore, boolean accountNonLocked,
                int failedAttempt) {
        this.password = password;
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.rolesToStore = rolesToStore;
        this.accountNonLocked = true;
        this.failedAttempt = failedAttempt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
