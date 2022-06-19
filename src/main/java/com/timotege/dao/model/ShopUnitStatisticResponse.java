package com.timotege.dao.model;

import java.util.List;

public class ShopUnitStatisticResponse {
    private List<ShopUnitStatisticUnit> items;

    public ShopUnitStatisticResponse(List<ShopUnitStatisticUnit> items) {
        this.items = items;
    }

    public List<ShopUnitStatisticUnit> getItems() {
        return items;
    }
}
