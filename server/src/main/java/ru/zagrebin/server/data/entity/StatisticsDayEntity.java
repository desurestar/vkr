package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "statistics_day", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "day_date"}))
public class StatisticsDayEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "day_date", nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private int waterConsumedMl = 0;

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getWaterConsumedMl() { return waterConsumedMl; }
    public void setWaterConsumedMl(int waterConsumedMl) { this.waterConsumedMl = waterConsumedMl; }
}
