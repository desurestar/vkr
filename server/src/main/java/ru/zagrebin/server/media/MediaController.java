package ru.zagrebin.server.media;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private final Path mediaDir;

    public MediaController(@Value("${app.media.upload-dir:media}") String uploadDir) {
        this.mediaDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaUploadResponse upload(@RequestPart("file") MultipartFile file,
                                      HttpSession session) throws IOException {
        requireUid(session);
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        var contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are supported");
        }

        Files.createDirectories(mediaDir);
        var filename = UUID.randomUUID() + extensionFor(contentType, file.getOriginalFilename());
        var target = mediaDir.resolve(filename).normalize();
        if (!target.startsWith(mediaDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }
        file.transferTo(target);

        return new MediaUploadResponse("/media/" + filename);
    }

    private void requireUid(HttpSession session) {
        var uid = (Long) session.getAttribute("uid");
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    private String extensionFor(String contentType, String originalFilename) {
        var originalExtension = originalFilename == null ? "" : originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/jpeg" -> originalExtension.equals("jpeg") ? ".jpeg" : ".jpg";
            default -> ".jpg";
        };
    }

    public record MediaUploadResponse(String url) {}
}
