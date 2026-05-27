package ru.zagrebin.server.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.zagrebin.server.data.entity.PostEntity;
import ru.zagrebin.server.data.entity.TagEntity;
import ru.zagrebin.server.data.entity.UserEntity;
import ru.zagrebin.server.data.repo.PostRepository;
import ru.zagrebin.server.data.repo.TagRepository;
import ru.zagrebin.server.data.repo.UserRepository;

import java.time.Instant;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository users;
    private final PostRepository posts;
    private final TagRepository tags;
    private final BCryptPasswordEncoder encoder;

    public DataInitializer(UserRepository users,
                           PostRepository posts,
                           TagRepository tags,
                           BCryptPasswordEncoder encoder) {
        this.users = users;
        this.posts = posts;
        this.tags = tags;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (tags.count() == 0) {
            List.of("Завтрак", "Обед", "Ужин", "Перекус", "ПП", "Веган", "Десерт", "Суп", "Салат", "Паста", "Мясо", "Рыба")
                    .forEach(name -> {
                        var tag = new TagEntity();
                        tag.setName(name);
                        tag.setLabel(name);
                        tag.setColor("#B57A1D");
                        tags.save(tag);
                    });
        }

        if (users.count() > 0) return;

        var demo = new UserEntity();
        demo.setUsername("demo");
        demo.setEmail("demo@example.com");
        demo.setPasswordHash(encoder.encode("demo12345"));
        demo.setDisplayName("Demo User");
        demo.setBio("Bio");

        demo = users.save(demo);

        var p = new PostEntity();
        p.setAuthor(demo);
        p.setType("RECIPE");
        p.setTitle("Паста с песто");
        p.setSummary("Быстрый ужин");
        p.setContent("Полный рецепт");
        p.setCreatedAt(Instant.now());
        tags.findByNameIgnoreCase("Ужин").ifPresent(t -> p.getTags().add(t));

        posts.save(p);
    }
}
