package dev.stormgui.webfluxcourse.service;

import dev.stormgui.webfluxcourse.entity.User;
import dev.stormgui.webfluxcourse.mapper.UserMapper;
import dev.stormgui.webfluxcourse.model.request.UserRequest;
import dev.stormgui.webfluxcourse.repository.UserRepository;
import dev.stormgui.webfluxcourse.service.exception.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Mono<User> save(final UserRequest request) {
        return userRepository.save(userMapper.toEntity(request));
    }

    public Mono<User> findById(final String id) {
        return handleNotFound(userRepository.findById(id), id);
    }

    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    public Mono<User> update(final String id, final UserRequest request) {
        return findById(id)
                .map(entity -> userMapper.toEntity(request, entity))
                .flatMap(userRepository::save);
    }

    public Mono<User> delete(final String id) {
        return handleNotFound(userRepository.findAndRemove(id), id);
    }

    private <T> Mono<T> handleNotFound(Mono<T> mono, String id) {
        return mono.switchIfEmpty(Mono.error(
                new ObjectNotFoundException(
                        format("Object not found. Id: %s, Type: %s", id, User.class.getSimpleName())
                )
        ));
    }
}
