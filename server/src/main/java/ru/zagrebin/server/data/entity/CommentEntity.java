package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comment")
public class CommentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private UserEntity author;
    @ManyToOne(optional = false)
    private PostEntity post;
    @Column(nullable = false)
    private String text;
    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public UserEntity getAuthor() { return author; }
    public void setAuthor(UserEntity author) { this.author = author; }
    public PostEntity getPost() { return post; }
    public void setPost(PostEntity post) { this.post = post; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
