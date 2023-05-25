package dev.stormgui.webfluxcourse.service;

import dev.stormgui.webfluxcourse.entity.User;
import dev.stormgui.webfluxcourse.mapper.UserMapper;
import dev.stormgui.webfluxcourse.model.request.UserRequest;
import dev.stormgui.webfluxcourse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Mono<User> save(final UserRequest request) {
        return userRepository.save(userMapper.toEntity(request));
    }

    public Mono<User> findById(final String id) {
        return userRepository.findById(id);
    }
}
