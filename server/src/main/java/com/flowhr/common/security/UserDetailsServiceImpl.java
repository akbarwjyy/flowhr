package com.flowhr.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JdbcTemplate jdbc;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Query user + roles
        String userSql = """
                SELECT u.id, u.username, u.password, u.is_active
                FROM users u
                WHERE u.username = ? AND u.is_active = true
                """;

        List<UserDetails> results = jdbc.query(userSql, (rs, rowNum) -> {
            Long userId = rs.getLong("id");

            // Load roles for this user
            String rolesSql = """
                    SELECT r.name FROM roles r
                    JOIN user_roles ur ON ur.role_id = r.id
                    WHERE ur.user_id = ?
                    """;
            List<SimpleGrantedAuthority> authorities = jdbc.query(
                    rolesSql, (rrs, rn) -> new SimpleGrantedAuthority(rrs.getString("name")), userId);

            return User.builder()
                    .username(rs.getString("username"))
                    .password(rs.getString("password"))
                    .authorities(authorities)
                    .build();
        }, username);

        if (results.isEmpty()) {
            throw new UsernameNotFoundException("User tidak ditemukan: " + username);
        }
        return results.get(0);
    }
}
