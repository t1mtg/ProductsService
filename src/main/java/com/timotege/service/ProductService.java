package com.timotege.service;

import com.timotege.dao.model.ShopUnit;
import com.timotege.dao.model.ShopUnitImportRequest;
import com.timotege.dao.model.ShopUnitStatisticResponse;

import java.time.LocalDateTime;

public interface ProductService {
    ShopUnitImportRequest importProducts(ShopUnitImportRequest shopUnitImportRequest);

    void deleteProduct(String id);

    ShopUnit getShopUnit(String id);

    void updateCategoryPrice(ShopUnit category, LocalDateTime date);

    ShopUnitStatisticResponse getUnitsWithPriceChanged(String date);

    ShopUnitStatisticResponse getStatistics(String id, String dateStart, String dateFinish);
}
