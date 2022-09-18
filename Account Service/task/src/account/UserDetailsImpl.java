package account;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UserDetailsImpl implements UserDetails {
    private final String username;
    private final String password;
    private final List<GrantedAuthority> rolesAndAuthorities;

    public User getUser() {
        return user;
    }

    private final User user;
    public UserDetailsImpl(User user) {
        username = user.getEmail();
        password = user.getPassword();
        rolesAndAuthorities = new ArrayList<>();
        this.user = user;
        Set<Group> userRoles = user.getRolesToStore();
        for (Group group : userRoles
        ) {
            rolesAndAuthorities.add(new SimpleGrantedAuthority(group.getRole()));
        }
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolesAndAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // 4 remaining methods that just return true
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
