package ru.zagrebin.server.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MediaResourceConfig implements WebMvcConfigurer {
    private final String mediaLocation;

    public MediaResourceConfig(@Value("${app.media.upload-dir:media}") String uploadDir) {
        var location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        this.mediaLocation = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**")
                .addResourceLocations(mediaLocation);
    }
}
