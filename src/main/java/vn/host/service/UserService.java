package vn.host.service;

import vn.host.entity.User;

import java.util.List;

public interface UserService {
    void save(User user);
    void delete(long id);
    List<User> findAll();
    User findByEmail(String email);
    User findByPhone(String phone);
    User findById(long id);
}
