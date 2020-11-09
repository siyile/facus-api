package top.siyile.facusapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import top.siyile.facusapi.controller.UserController;
import top.siyile.facusapi.model.User;
import top.siyile.facusapi.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = { "spring.config.location=classpath:application.yml" })
@Slf4j
public class UserTest {
    private final UserController controller;
    @Autowired
    public UserTest(UserController controller) {
        this.controller = controller;
    }

    @Test
    public void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    public void counter(@Autowired UserRepository repository) {
        repository.save(new User("user", "password"));
    }
}
