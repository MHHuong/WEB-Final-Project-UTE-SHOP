package vn.host.service;

import vn.host.entity.User;
import vn.host.model.request.UserRequest;

import java.util.List;

public interface UserService {
    User getUserByEmail(String email);

    User findByEmail(String email);

    void save(User user);


    User saveInfoUser(UserRequest userRequest, Long userId);
}
