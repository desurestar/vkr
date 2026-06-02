package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
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
    @Column(length = 2048)
    private String imageUrl;
    private int likes;
    private Integer cookTimeMinutes;
    @Column(name = "proteins_per_100")
    private BigDecimal proteinsPer100;
    @Column(name = "fats_per_100")
    private BigDecimal fatsPer100;
    @Column(name = "carbs_per_100")
    private BigDecimal carbsPer100;
    @Column(name = "kcal_per_100")
    private BigDecimal kcalPer100;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private String status = "PUBLISHED";

    @ManyToMany
    @JoinTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<TagEntity> tags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredientEntity> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<RecipeStepEntity> steps = new ArrayList<>();

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
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public Integer getCookTimeMinutes() { return cookTimeMinutes; }
    public void setCookTimeMinutes(Integer cookTimeMinutes) { this.cookTimeMinutes = cookTimeMinutes; }
    public BigDecimal getProteinsPer100() { return proteinsPer100; }
    public void setProteinsPer100(BigDecimal proteinsPer100) { this.proteinsPer100 = proteinsPer100; }
    public BigDecimal getFatsPer100() { return fatsPer100; }
    public void setFatsPer100(BigDecimal fatsPer100) { this.fatsPer100 = fatsPer100; }
    public BigDecimal getCarbsPer100() { return carbsPer100; }
    public void setCarbsPer100(BigDecimal carbsPer100) { this.carbsPer100 = carbsPer100; }
    public BigDecimal getKcalPer100() { return kcalPer100; }
    public void setKcalPer100(BigDecimal kcalPer100) { this.kcalPer100 = kcalPer100; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<TagEntity> getTags() { return tags; }
    public List<RecipeIngredientEntity> getIngredients() { return ingredients; }
    public List<RecipeStepEntity> getSteps() { return steps; }
    public List<CommentEntity> getComments() { return comments; }
}
