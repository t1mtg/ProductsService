package com.timotege.dao.model;

import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ShopUnit {
    @Id
    @Column(name = "id")
    @NotNull
    private UUID id;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "date")
    @NotNull
    private LocalDateTime date;

    @Column(name = "parent_id")
    @Nullable
    private UUID parentId;

    @Column(name = "type")
    @NotNull
    @Enumerated
    private ShopUnitType type;

    @Column(name = "price")
    private Integer price;
    @OneToMany(mappedBy = "parentId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopUnit> children = new ArrayList<>();

    public ShopUnit(UUID id, @Nullable UUID parentId, String name, LocalDateTime date, ShopUnitType type, Integer price) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.date = date;
        this.type = type;
        this.price = price;
    }


    public ShopUnit() {

    }

    public List<ShopUnit> getChildren() {
        return children;
    }

    public void setChildren(List<ShopUnit> children) {
        this.children = children;
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public ShopUnitType getType() {
        return type;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

}
