package com.timotege.dao.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "history")
public class ShopUnitStatisticUnit {
    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long recordId;

    @NotNull
    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "parent_id")
    private UUID parentId;

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "type")
    private ShopUnitType type;

    @Column(name = "price")
    private Integer price;

    @NotNull
    @Column(name = "date")
    private LocalDateTime dateTime;

    public ShopUnitStatisticUnit(UUID id, UUID parentId, String name, ShopUnitType type, Integer price, LocalDateTime dateTime) {
        this.unitId = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.price = price;
        this.dateTime = dateTime;
    }

    public ShopUnitStatisticUnit() {
    }

    public UUID getId() {
        return unitId;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public ShopUnitType getType() {
        return type;
    }

    public Integer getPrice() {
        return price;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
