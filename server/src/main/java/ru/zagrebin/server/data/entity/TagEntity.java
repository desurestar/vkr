package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tag")
public class TagEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private String label;
    private String color;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
