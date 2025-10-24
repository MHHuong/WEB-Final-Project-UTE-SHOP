package vn.host.service;

import vn.host.entity.User;

import java.util.List;

public interface UserService {
    User getUserByEmail(String email);
}
