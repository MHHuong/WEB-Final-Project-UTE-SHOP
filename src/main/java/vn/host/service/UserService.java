package vn.host.service;

import vn.host.entity.User;

import java.util.List;

public interface UserService {
    User getUserByEmail(String email);

    User findByEmail(String email);

    void save(User user);
}
