package com.timotege.service;

import com.timotege.dao.model.*;
import com.timotege.dao.repository.HistoryRepository;
import com.timotege.dao.repository.ProductRepository;
import com.timotege.exception.InvalidDataException;
import com.timotege.exception.ItemNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final HistoryRepository historyRepository;

    public ProductServiceImpl(ProductRepository productRepository, HistoryRepository historyRepository) {
        this.productRepository = productRepository;
        this.historyRepository = historyRepository;
    }

    private static boolean isIsoDate(String date) {
        try {
            LocalDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(date));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public ShopUnitImportRequest importProducts(ShopUnitImportRequest shopUnitImportRequest) {
        List<ShopUnit> shopUnits = new ArrayList<>();
        List<ShopUnitStatisticUnit> shopStatisticUnits = new ArrayList<>();
        for (ShopUnitImport shopUnitImport : shopUnitImportRequest.getItems()) {

            validateShopUnit(shopUnitImport, shopUnitImportRequest.getUpdateDate());

            ShopUnit shopUnit = new ShopUnit(
                    shopUnitImport.getId(),
                    shopUnitImport.getParentId(),
                    shopUnitImport.getName(),
                    LocalDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME
                            .parse(shopUnitImportRequest.getUpdateDate())),
                    shopUnitImport.getType(),
                    shopUnitImport.getPrice());

            shopUnits.add(shopUnit);
            shopStatisticUnits.add((new ShopUnitStatisticUnit(
                    shopUnitImport.getId(),
                    shopUnitImport.getParentId(),
                    shopUnitImport.getName(),
                    shopUnitImport.getType(),
                    shopUnitImport.getPrice(),
                    shopUnit.getDateInLocalDateTimeFormat()
            )));
        }

        productRepository.saveAll(shopUnits);
        historyRepository.saveAll(shopStatisticUnits);
        shopUnits.stream()
                .filter(shopUnit -> shopUnit.getType().equals(ShopUnitType.OFFER))
                .forEach(shopUnit -> updateCategoryPrice(shopUnit, shopUnit.getDateInLocalDateTimeFormat()));
        return shopUnitImportRequest;
    }

    private void validateShopUnit(ShopUnitImport shopUnitImport, String date) {
        if (shopUnitImport.getId() == null
                || shopUnitImport.getName() == null
                || shopUnitImport.getType() == null) {
            throw new InvalidDataException("Invalid argument");
        }

        if (shopUnitImport.getType().equals(ShopUnitType.CATEGORY) && shopUnitImport.getPrice() != null) {
            throw new InvalidDataException("Category price should be null");
        }

        if (!isIsoDate(date)) {
            throw new InvalidDataException("Date is not in ISO8601 format");
        }

        if (shopUnitImport.getType().equals(ShopUnitType.OFFER) &&
                (shopUnitImport.getPrice() == null
                        || shopUnitImport.getPrice() < 0)) {
            throw new InvalidDataException("Offer price should be greater than null");
        }


        if (shopUnitImport.getParentId() != null
                && productRepository.findById(shopUnitImport.getParentId()).isPresent()) {
            ShopUnit parent = productRepository.findById(shopUnitImport.getParentId()).get();
            if (parent.getType().equals(ShopUnitType.OFFER)) {
                throw new InvalidDataException("offer could not be a parent");
            }
        }
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            var shopUnit = getShopUnit(id);
            if (shopUnit == null) {
                throw new ItemNotFoundException("Item not found");
            }
            productRepository.deleteById(uuid);
            historyRepository.deleteAllByUnitId(uuid);
            if (shopUnit.getParentId() != null)
                updateCategoryPrice(productRepository.findById(shopUnit.getParentId()).get(), null);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Cannot convert id to UUID");
        }
    }

    @Override
    public ShopUnit getShopUnit(String id) {

        try {
            UUID uuid = UUID.fromString(id);
            ShopUnit shopUnit = productRepository.findById(uuid).orElse(null);
            if (shopUnit == null) {
                throw new ItemNotFoundException("Item not found");
            }

            updateNullChildrenForOffer(shopUnit);
            System.out.println(shopUnit.getDate());
            return shopUnit;
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Cannot convert id to UUID");
        }

    }

    @Override
    public void updateCategoryPrice(ShopUnit shopUnit, LocalDateTime date) {
        if (shopUnit.getType().equals(ShopUnitType.CATEGORY)) {
            Integer avgValue = null;
            if (productRepository.findByParentId(shopUnit.getId()).size() > 0) {
                var children = productRepository.findByParentId(shopUnit.getId());
                Integer sum = 0;
                int count = 0;
                for (var ch : children) {
                    if (ch.getType().equals(ShopUnitType.OFFER)) {
                        sum += ch.getPrice();
                        count++;
                    } else {
                        var cnt = productRepository.findByParentId(ch.getId()).size();
                        if (cnt != 0) {
                            sum += ch.getPrice() * cnt;
                            count += cnt;
                        }
                    }
                }

                if (count != 0)
                    avgValue = sum / count;

            }
            shopUnit.setPrice(avgValue);
            if (date != null)
                shopUnit.setDate(date);
            productRepository.save(shopUnit);
        }

        if (shopUnit.getParentId() != null && productRepository.findById(shopUnit.getParentId()).isPresent()) {
            updateCategoryPrice(getShopUnit(shopUnit.getParentId().toString()), date);
        }
    }

    @Override
    public ShopUnitStatisticResponse getUnitsWithPriceChanged(String date) {
        if (!isIsoDate(date)) {
            throw new InvalidDataException("Date is not in ISO8601 format");
        }
        LocalDateTime currentDate = LocalDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(date));
        LocalDateTime dateBefore = currentDate.minusDays(1);
        List<ShopUnitStatisticUnit> essentialUnits = productRepository
                .findByDateIsBetween(dateBefore, currentDate).stream()
                .map(value -> new ShopUnitStatisticUnit(value.getId(),
                        value.getParentId(),
                        value.getName(),
                        value.getType(),
                        value.getPrice(),
                        value.getDateInLocalDateTimeFormat()))
                .toList();
        return new ShopUnitStatisticResponse(essentialUnits);
    }

    @Override
    public ShopUnitStatisticResponse getStatistics(String id, String dateS, String dateF) {
        if (!isIsoDate(dateS) || !isIsoDate(dateS)) {
            throw new InvalidDataException("Date is not in ISO8601 format");
        }
        LocalDateTime dateStart = LocalDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(dateS));
        LocalDateTime dateFinish = LocalDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(dateF));
        List<ShopUnitStatisticUnit> essentialUnits = historyRepository
                .findByDateTimeGreaterThanEqualAndDateTimeLessThanAndUnitId(dateStart, dateFinish, UUID.fromString(id));
        if (essentialUnits.isEmpty())
            throw new ItemNotFoundException("Item not found");
        return new ShopUnitStatisticResponse(essentialUnits);
    }

    private void updateNullChildrenForOffer(ShopUnit shopUnit) {
        if (shopUnit.getType().equals(ShopUnitType.OFFER)) {
            shopUnit.setChildren(null);
        } else {
            productRepository
                    .findByParentId(shopUnit.getId())
                    .forEach(this::updateNullChildrenForOffer);
        }
    }
}
