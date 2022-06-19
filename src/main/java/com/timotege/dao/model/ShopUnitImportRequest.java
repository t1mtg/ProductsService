package com.timotege.dao.model;

import java.util.List;

public class ShopUnitImportRequest {
    private List<ShopUnitImport> items;
    private String updateDate;

    public ShopUnitImportRequest(List<ShopUnitImport> items, String updateDate) {
        this.items = items;
        this.updateDate = updateDate;
    }

    public List<ShopUnitImport> getItems() {
        return items;
    }

    public String getUpdateDate() {
        return updateDate;
    }

}
