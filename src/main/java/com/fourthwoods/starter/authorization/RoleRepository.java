package com.fourthwoods.starter.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RoleRepository {
  final static Logger logger = LoggerFactory.getLogger(RoleRepository.class);

  private String ROLE_FIELDS = " id, role, deleted, date_created, last_updated, version ";
  private String TABLE = " roles ";

  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  public RoleRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public Role getRole(String id) {
    MapSqlParameterSource params = new MapSqlParameterSource();

    String sql = "SELECT " + ROLE_FIELDS + " FROM " + TABLE + " WHERE id = :id";

    params.addValue("id", id);

    return namedParameterJdbcTemplate.queryForObject(sql, params, new RoleRowMapper());
  }

  public Role getRoleByRole(String role) {
    MapSqlParameterSource params = new MapSqlParameterSource();

    String sql = "SELECT " + ROLE_FIELDS + " FROM " + TABLE + " WHERE role = :role";

    params.addValue("role", role);

    return namedParameterJdbcTemplate.queryForObject(sql, params, new RoleRowMapper());
  }

  public List<Role> getRoles() {
    MapSqlParameterSource params = new MapSqlParameterSource();

    String sql = "SELECT " + ROLE_FIELDS + " FROM " + TABLE;

    return namedParameterJdbcTemplate.query(sql, params, new RoleRowMapper());
  }

  public static class RoleRowMapper implements RowMapper<Role>
  {
    @Override
    public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
      Role role = new Role();

      role.setId(rs.getString("id"));
      role.setRole(rs.getString("role"));
      role.setDeleted(rs.getBoolean("deleted"));
      role.setDateCreated(rs.getTimestamp("date_created"));
      role.setLastUpdated(rs.getTimestamp("last_updated"));
      role.setVersion(rs.getInt("version"));

      return role;
    }
  }
}
