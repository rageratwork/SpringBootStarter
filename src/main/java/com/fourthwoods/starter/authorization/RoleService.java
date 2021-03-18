package com.fourthwoods.starter.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
  final static Logger logger = LoggerFactory.getLogger(RoleRepository.class);

  private RoleRepository roleRepository;

  @Autowired
  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public Role getRole(String id) {
    return roleRepository.getRole(id);
  }

  public Role getRoleByRole(String role) {
    return roleRepository.getRoleByRole(role);
  }

  public List<Role> getRoles() {
    return roleRepository.getRoles();
  }

}
