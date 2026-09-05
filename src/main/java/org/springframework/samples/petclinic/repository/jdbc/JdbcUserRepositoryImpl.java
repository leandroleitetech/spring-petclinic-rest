package org.springframework.samples.petclinic.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.samples.petclinic.model.Role;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.repository.UserRepository;
import org.springframework.stereotype.Repository;

@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
public class JdbcUserRepositoryImpl implements UserRepository {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private SimpleJdbcInsert insertUser;

    public JdbcUserRepositoryImpl(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.insertUser = new SimpleJdbcInsert(dataSource).withTableName("users");
    }

    @Override
    public User findByUsername(String username) throws DataAccessException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            User user = this.namedParameterJdbcTemplate.queryForObject("SELECT * FROM users WHERE username=:username",
                params, new UserRowMapper());
            loadUserRoles(user);
            return user;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public User findByResetToken(String token) throws DataAccessException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("token", token);
            User user = this.namedParameterJdbcTemplate.queryForObject("SELECT * FROM users WHERE reset_token=:token",
                params, new UserRowMapper());
            loadUserRoles(user);
            return user;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void save(User user) throws DataAccessException {

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("username", user.getUsername())
            .addValue("password", user.getPassword())
            .addValue("enabled", user.getEnabled())
            .addValue("reset_token", user.getResetToken())
            .addValue("reset_token_expiry", toTimestamp(user.getResetTokenExpiry()));

        User existing = findByUsername(user.getUsername());
        if (existing != null) {
            this.namedParameterJdbcTemplate.update(
                "UPDATE users SET password=:password, enabled=:enabled, reset_token=:reset_token, reset_token_expiry=:reset_token_expiry WHERE username=:username",
                parameterSource);
        } else {
            this.insertUser.execute(parameterSource);
        }
        updateUserRoles(user);
    }

    private Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private void loadUserRoles(User user) {
        if (user == null) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("username", user.getUsername());
        List<Role> roles = this.namedParameterJdbcTemplate.query("SELECT * FROM roles WHERE username=:username", params,
            (rs, rowNum) -> {
                Role role = new Role();
                role.setName(rs.getString("role"));
                role.setUser(user);
                return role;
            });
        user.setRoles(new HashSet<>(roles));
    }

    private void updateUserRoles(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", user.getUsername());
        this.namedParameterJdbcTemplate.update("DELETE FROM roles WHERE username=:username", params);
        Set<Role> roles = user.getRoles();
        if (roles != null) {
            for (Role role : roles) {
                params.put("role", role.getName());
                if (role.getName() != null) {
                    this.namedParameterJdbcTemplate.update("INSERT INTO roles(username, role) VALUES (:username, :role)", params);
                }
            }
        }
    }

    private static class UserRowMapper implements RowMapper<User> {

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEnabled(rs.getBoolean("enabled"));
            user.setResetToken(rs.getString("reset_token"));
            Timestamp expiry = rs.getTimestamp("reset_token_expiry");
            user.setResetTokenExpiry(expiry == null ? null : expiry.toInstant());
            return user;
        }
    }
}
