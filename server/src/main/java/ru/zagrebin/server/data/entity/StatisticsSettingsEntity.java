package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "statistics_settings")
public class StatisticsSettingsEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;
    @Column(nullable = false)
    private int retentionMonths = 3;
    @Column(nullable = false)
    private int goalKcal = 2000;
    @Column(nullable = false)
    private int waterGoalMl = 1500;
    @Column(nullable = false)
    private int proteinGoalGrams = 90;
    @Column(nullable = false)
    private int fatGoalGrams = 70;
    @Column(nullable = false)
    private int carbsGoalGrams = 250;

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public int getRetentionMonths() { return retentionMonths; }
    public void setRetentionMonths(int retentionMonths) { this.retentionMonths = retentionMonths; }
    public int getGoalKcal() { return goalKcal; }
    public void setGoalKcal(int goalKcal) { this.goalKcal = goalKcal; }
    public int getWaterGoalMl() { return waterGoalMl; }
    public void setWaterGoalMl(int waterGoalMl) { this.waterGoalMl = waterGoalMl; }
    public int getProteinGoalGrams() { return proteinGoalGrams; }
    public void setProteinGoalGrams(int proteinGoalGrams) { this.proteinGoalGrams = proteinGoalGrams; }
    public int getFatGoalGrams() { return fatGoalGrams; }
    public void setFatGoalGrams(int fatGoalGrams) { this.fatGoalGrams = fatGoalGrams; }
    public int getCarbsGoalGrams() { return carbsGoalGrams; }
    public void setCarbsGoalGrams(int carbsGoalGrams) { this.carbsGoalGrams = carbsGoalGrams; }
}
