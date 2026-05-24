package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
public class PostEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private UserEntity author;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private String title;
    private String summary;
    @Column(columnDefinition = "TEXT")
    private String content;
    private int likes;
    @Column(nullable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();

    public Long getId() { return id; }
    public UserEntity getAuthor() { return author; }
    public void setAuthor(UserEntity author) { this.author = author; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<String> getTags() { return tags; }
    public List<CommentEntity> getComments() { return comments; }
}
