package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.host.entity.User;
import vn.host.repository.UserRepository;
import vn.host.service.UserService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    @Override
    public void save(User user) {
        userRepo.save(user);
    }


}
