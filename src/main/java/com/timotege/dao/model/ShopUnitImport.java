package com.timotege.dao.model;

import com.sun.istack.NotNull;

import java.util.UUID;

public class ShopUnitImport {
    @NotNull
    private UUID id;

    private UUID parentId;

    private String name;

    private ShopUnitType type;


    private Integer price;

    public ShopUnitImport(UUID id, UUID parentId, String name, ShopUnitType type, Integer price) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.price = price;
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

    public ShopUnitType getType() {
        return type;
    }

    public Integer getPrice() {
        return price;
    }
}
