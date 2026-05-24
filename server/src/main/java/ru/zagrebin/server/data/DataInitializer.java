package ru.zagrebin.server.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.zagrebin.server.data.entity.PostEntity;
import ru.zagrebin.server.data.entity.UserEntity;
import ru.zagrebin.server.data.repo.PostRepository;
import ru.zagrebin.server.data.repo.UserRepository;

import java.time.Instant;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final PostRepository posts;
    private final DbService db;

    public DataInitializer(UserRepository users, PostRepository posts, DbService db) {
        this.users = users; this.posts = posts; this.db = db;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;
        var demo = new UserEntity();
        demo.setUsername("demo"); demo.setEmail("demo@example.com"); demo.setPasswordHash(db.encoder.encode("demo12345"));
        demo.setDisplayName("Demo User"); demo.setBio("Bio");
        demo = users.save(demo);

        var p = new PostEntity();
        p.setAuthor(demo); p.setType("RECIPE"); p.setTitle("Паста с песто"); p.setSummary("Быстрый ужин"); p.setContent("Полный рецепт");
        p.setCreatedAt(Instant.now()); p.getTags().addAll(List.of("ужин"));
        posts.save(p);
    }
}
