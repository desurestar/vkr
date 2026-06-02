package ru.zagrebin.server.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shopping_item")
public class ShoppingItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private UserEntity user;
    @ManyToOne(optional = false)
    private ShoppingListEntity list;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String amount;
    @Column(nullable = false)
    private boolean checked;

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public ShoppingListEntity getList() { return list; }
    public void setList(ShoppingListEntity list) { this.list = list; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
