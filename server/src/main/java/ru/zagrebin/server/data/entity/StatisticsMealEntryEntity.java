package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "statistics_meal_entry")
public class StatisticsMealEntryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "day_date", nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String amountLabel;
    @Column(nullable = false)
    private String timeLabel;
    @Column(nullable = false)
    private int kcal;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal proteins;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fats;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal carbs;
    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAmountLabel() { return amountLabel; }
    public void setAmountLabel(String amountLabel) { this.amountLabel = amountLabel; }
    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
    public int getKcal() { return kcal; }
    public void setKcal(int kcal) { this.kcal = kcal; }
    public BigDecimal getProteins() { return proteins; }
    public void setProteins(BigDecimal proteins) { this.proteins = proteins; }
    public BigDecimal getFats() { return fats; }
    public void setFats(BigDecimal fats) { this.fats = fats; }
    public BigDecimal getCarbs() { return carbs; }
    public void setCarbs(BigDecimal carbs) { this.carbs = carbs; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
