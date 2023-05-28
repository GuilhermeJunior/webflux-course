package dev.stormgui.webfluxcourse.controller;

import com.mongodb.reactivestreams.client.MongoClient;
import dev.stormgui.webfluxcourse.entity.User;
import dev.stormgui.webfluxcourse.mapper.UserMapper;
import dev.stormgui.webfluxcourse.model.request.UserRequest;
import dev.stormgui.webfluxcourse.service.UserService;
import dev.stormgui.webfluxcourse.service.exception.ObjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
class UserControllerImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService service;

    @SpyBean
    private UserMapper mapper;

    @MockBean
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @MockBean
    private MongoClient mongoClient;

    @MockBean
    private ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory;


    @Test
    @DisplayName("Test endpoint save with success")
    void testSaveWithSuccess() {
        UserRequest request = new UserRequest("Guilherme", "guilherme@email.com", "123");

        when(service.save(any(UserRequest.class))).thenReturn(Mono.just(User.builder().build()));
        webTestClient.post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isCreated();

        verify(service, times(1)).save(any(UserRequest.class));
    }

    @Test
    @DisplayName("Test endpoint save with bad request")
    void testSaveWithBadRequest() {
        UserRequest request = new UserRequest(" Guilherme", "guilherme@email.com", "123");

        webTestClient.post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/users")
                .jsonPath("$.status").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.error").isEqualTo("Validation ERROR")
                .jsonPath("$.message").isEqualTo("Validation Error on validation attributes")
                .jsonPath("$.errors[0].fieldName").isEqualTo("name")
                .jsonPath("$.errors[0].message").isEqualTo("field cannot have blank space at the beginning or at the end")
        ;
    }

    @Test
    @DisplayName("Test find by id endpoint with sucess")
    void findByIdWithSuccess() {
        var user = User.builder()
                .id("testID")
                .name("Guilherme")
                .email("guilherm@email.com")
                .password("123456")
                .build();

        when(service.findById(anyString())).thenReturn(Mono.just(user));

        webTestClient.get().uri("/users/" + "testID")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(user.getId())
                .jsonPath("$.name").isEqualTo(user.getName())
                .jsonPath("$.password").isEqualTo(user.getPassword())
                .jsonPath("$.email").isEqualTo(user.getEmail())
        ;
    }

    @Test
    void findByIdWithException() {
//        when(service.findById(anyString())).thenReturn(Mono.empty()); // won't work because I would need to mock the repository
        when(service.findById(anyString())).thenThrow(new ObjectNotFoundException(""));

        webTestClient.get().uri("/users/" + "12345")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    @DisplayName("Test find All endpoint with success")
    void findAllWithSuccess() {
        var user = User.builder()
                .id("testID")
                .name("Guilherme")
                .email("guilherm@email.com")
                .password("123456")
                .build();

        when(service.findAll()).thenReturn(Flux.just(user));

        webTestClient.get().uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(user.getId())
                .jsonPath("$[0].name").isEqualTo(user.getName())
                .jsonPath("$[0].password").isEqualTo(user.getPassword())
                .jsonPath("$[0].email").isEqualTo(user.getEmail())
        ;
    }

    @Test
    void update() {
        var request = new UserRequest("Guilherme", "guilherme@email.com", "123");
        var user = User.builder()
                .id("testID")
                .name("Guilherme")
                .email("guilherm@email.com")
                .password("123456")
                .build();

        when(service.update(anyString(), any(UserRequest.class)))
                .thenReturn(Mono.just(user));

        webTestClient.patch().uri("/users/" + "testID")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(user.getId())
                .jsonPath("$.name").isEqualTo(user.getName())
                .jsonPath("$.password").isEqualTo(user.getPassword())
                .jsonPath("$.email").isEqualTo(user.getEmail())
        ;

        verify(service).update(anyString(), any(UserRequest.class));
    }

    @Test
    void delete() {
        var user = User.builder()
                .id("testID")
                .name("Guilherme")
                .email("guilherm@email.com")
                .password("123456")
                .build();

        when(service.delete(anyString())).thenReturn(Mono.just(user));

        webTestClient.delete().uri("/users/" + user.getId())
                .exchange()
                .expectStatus().isOk();

        verify(service).delete(anyString());
    }
}