package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredientEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private PostEntity post;
    @Column(nullable = false)
    private String name;
    private BigDecimal amount;
    private String unit;

    public Long getId() { return id; }
    public PostEntity getPost() { return post; }
    public void setPost(PostEntity post) { this.post = post; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
