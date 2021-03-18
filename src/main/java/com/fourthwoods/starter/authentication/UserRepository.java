package com.fourthwoods.starter.authentication;

import com.fourthwoods.starter.authorization.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class UserRepository {
    final static Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private String SELECT_FIELDS = " u.id, u.username, u.email, u.first_name, u.last_name, u.password, u.password_expired, " +
            "u.enabled, u.locked, u.deleted, u.date_created, u.last_updated, u.version, r.role ";

    private String INSERT_FIELDS = " (id, username, email, first_name, last_name, password, password_expired) ";
    private String INSERT_VALUES = " (:id, :username, :email, :firstName, :lastName, :password, :passwordExpired) ";

    private String UPDATE_FIELDS = " id = :id, username = :username, email = :email, first_name = :firstName, last_name = :lastName, " +
        " password = :password, password_expired = :passwordExpired, enabled = :enabled, locked = :locked, deleted = :deleted, " +
        " date_created = :dateCreated, last_updated = :lastUpdated, version = :version ";

    private String TABLE = " users ";

    private String TABLES = " users u LEFT JOIN user_role ur ON u.id = ur.user_id LEFT JOIN roles r ON ur.role_id = r.id ";

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        logger.debug("Wiring NamedParameterJdbcTemplate " + namedParameterJdbcTemplate);

        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public User getUserByUsername(String username) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = "SELECT " + SELECT_FIELDS + " FROM " + TABLES + " WHERE " + "u.username = :username";

        params.addValue("username", username);

        List<User> users = namedParameterJdbcTemplate.query(sql, params, new UserResultSetExtractor());

        if(!users.isEmpty()) {
            return users.get(0);
        }

        return null;
    }

    public User getUser(String id) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = "SELECT " + SELECT_FIELDS + " FROM " + TABLES + " WHERE u.id = :id";

        params.addValue("id", id);

        List<User> users = namedParameterJdbcTemplate.query(sql, params, new UserResultSetExtractor());

        if(!users.isEmpty()) {
            return users.get(0);
        }

        return null;
    }

    public List<User> getUsers() {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = "SELECT " + SELECT_FIELDS + " FROM " + TABLES;

        return namedParameterJdbcTemplate.query(sql, params, new UserResultSetExtractor());
    }

    public User createUser (User user) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = "INSERT INTO " + TABLE + INSERT_FIELDS + " VALUES " + INSERT_VALUES;

        params.addValue("id", user.getId());
        params.addValue("username", user.getUsername());
        params.addValue("email", user.getEmail());
        params.addValue("firstName", user.getFirstName());
        params.addValue("lastName", user.getLastName());
        params.addValue("password", user.getPassword());
        params.addValue("passwordExpired", user.isPasswordExpired());

        int rows = namedParameterJdbcTemplate.update(sql, params);

        return getUser(user.getId());
    }

    public void updateUserRoles(User user, List<Role> roles) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String sql = "DELETE FROM user_role WHERE user_id = :userId";

        params.addValue("userId", user.getId());

        namedParameterJdbcTemplate.update(sql, params);

        for(Role role : roles) {
            sql = "INSERT INTO user_role (user_id, role_id) VALUES (:userId, :roleId)";
            params.addValue("roleId", role.getId());
            namedParameterJdbcTemplate.update(sql, params);
        }
    }

    public User updateUser(User user) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        Date now = new Date();

        String sql = "UPDATE " + TABLE + " SET " + UPDATE_FIELDS + " WHERE id = :id";

        params.addValue("id", user.getId());
        params.addValue("username", user.getUsername());
        params.addValue("email", user.getEmail());
        params.addValue("firstName", user.getFirstName());
        params.addValue("lastName", user.getLastName());
        params.addValue("password", user.getPassword());
        params.addValue("passwordExpired", user.isPasswordExpired());
        params.addValue("enabled", user.isEnabled());
        params.addValue("locked", user.isLocked());
        params.addValue("deleted", user.isDeleted());
        params.addValue("dateCreated", user.getDateCreated());
        params.addValue("lastUpdated", now);
        params.addValue("version", user.getVersion());

        int rows = namedParameterJdbcTemplate.update(sql, params);

        return getUser(user.getId());
    }

    public static class UserResultSetExtractor implements ResultSetExtractor<List<User>> {
        @Override
        public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<User> users = new ArrayList<>();
            UserRowMapper userRowMapper = new UserRowMapper();

            int row = 0;
            User user = null;
            while(rs.next()) {
                String currentId = rs.getString("id");
                if(user == null || !user.getId().equals(currentId)) {
                    user = userRowMapper.mapRow(rs, row);
                    user.setRoles(new ArrayList<>());
                }

                String role = rs.getString("role");
                if(role != null) {
                    user.getRoles().add(role);
                }

                users.add(user);

                row++;
            }

            return users;
        }
    }

    public static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();

            user.setId(rs.getString("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setPassword(rs.getString("password"));
            user.setPasswordExpired(rs.getBoolean("password_expired"));
            user.setEnabled(rs.getBoolean("enabled"));
            user.setLocked(rs.getBoolean("locked"));
            user.setDeleted(rs.getBoolean("deleted"));
            user.setDateCreated(rs.getTimestamp("date_created"));
            user.setLastUpdated(rs.getTimestamp("last_updated"));
            user.setVersion(rs.getInt("version"));

            String role = rs.getString("role");
            if(role != null) {
                user.getRoles().add(role);
            }

            return user;
        }
    }
}
