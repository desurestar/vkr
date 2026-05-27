package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_step")
public class RecipeStepEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private PostEntity post;
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    private String imageUrl;

    public Long getId() { return id; }
    public PostEntity getPost() { return post; }
    public void setPost(PostEntity post) { this.post = post; }
    public Integer getStepNumber() { return stepNumber; }
    public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
