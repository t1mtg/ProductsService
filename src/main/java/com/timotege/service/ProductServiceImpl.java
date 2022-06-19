package com.timotege.service;

import com.timotege.dao.model.*;
import com.timotege.dao.repository.ProductRepository;
import com.timotege.exception.InvalidDataException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final HashMap<UUID, List<ShopUnitStatisticUnit>> history;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.history = new HashMap<>();
    }

    @Override
    public ShopUnitImportRequest importProducts(ShopUnitImportRequest shopUnitImportRequest) {
        for (ShopUnitImport shopUnitImport : shopUnitImportRequest.getItems()) {

            if (shopUnitImport.getType().equals(ShopUnitType.CATEGORY) && shopUnitImport.getPrice() != null) {
                throw new InvalidDataException("Category price should be null");
            }

            if (!isIsoDate(shopUnitImportRequest.getUpdateDate())) {
                throw new InvalidDataException("Date is not in ISO8601 format");
            }

            if (shopUnitImport.getType().equals(ShopUnitType.OFFER) &&
                    (shopUnitImport.getPrice() == null || shopUnitImport.getPrice() < 0)) {
                throw new InvalidDataException("Offer price should be greater than null");
            }
            if (shopUnitImport.getId() == null || shopUnitImport.getName() == null || shopUnitImport.getType() == null) {
                throw new InvalidDataException("Invalid argument");
            }

            if (shopUnitImport.getParentId() != null && productRepository.findById(shopUnitImport.getParentId()).isPresent()) {
                ShopUnit parent = productRepository.findById(shopUnitImport.getParentId()).get();
                if (parent.getType().equals(ShopUnitType.OFFER)) {
                    throw new InvalidDataException("offer could not be a parent");
                }
            }

            ShopUnit shopUnit = new ShopUnit(
                    shopUnitImport.getId(),
                    shopUnitImport.getParentId(),
                    shopUnitImport.getName(),
                    LocalDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(shopUnitImportRequest.getUpdateDate())),
                    shopUnitImport.getType(),
                    shopUnitImport.getPrice());
            //TODO date validation DONE
            //TODO check 2 units with the same id in query
            //TODO empty list of children in Offer
            if (history.containsKey(shopUnitImport.getId())) {
                history.get(shopUnitImport.getId()).add(new ShopUnitStatisticUnit(
                        shopUnitImport.getId(),
                        shopUnitImport.getParentId(),
                        shopUnitImport.getName(),
                        shopUnitImport.getType(),
                        shopUnitImport.getPrice(),
                        LocalDateTime.from(
                                DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(shopUnitImportRequest.getUpdateDate()))));
            } else {
                history.put(shopUnit.getId(), new ArrayList<>());
            }

            productRepository.save(shopUnit);
            updateCategoryPrice(getShopUnit(shopUnit.getId().toString()), shopUnit.getDate());
        }

        return shopUnitImportRequest;
    }

    @Override
    public void deleteProduct(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            productRepository.deleteById(uuid);
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
                throw new EmptyResultDataAccessException("Item not found", 1);
            }

            getNullChildrenForOffer(shopUnit);

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
                Integer count = 0;
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
                /*avgValue = productRepository.findByParentId(shopUnit.getId()).stream()
                        .filter(shopUnit1 -> shopUnit1.getType().equals(ShopUnitType.OFFER))
                        .mapToInt(ShopUnit::getPrice).sum() / productRepository.findByParentId(shopUnit.getId()).size();*/

            }
            shopUnit.setPrice(avgValue);
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
        List<ShopUnitStatisticUnit> essentialUnits = productRepository.findByDateGreaterThanEqualAndDateLessThanEqual(dateBefore, currentDate).stream()
                .map(value -> new ShopUnitStatisticUnit(
                        value.getId(),
                        value.getParentId(),
                        value.getName(),
                        value.getType(),
                        value.getPrice(),
                        value.getDate()))
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
        List<ShopUnitStatisticUnit> essentialUnits = history.get(UUID.fromString(id))
                .stream().filter(o -> o.getDateTime().isAfter(dateStart) || o.getDateTime().equals(dateStart))
                .filter(o -> o.getDateTime().isBefore(dateFinish))
                .toList();
        return new ShopUnitStatisticResponse(essentialUnits);
    }

    private static boolean isIsoDate(String date) {
        try {
            LocalDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(date));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void getNullChildrenForOffer(ShopUnit shopUnit) {
        if (shopUnit.getType().equals(ShopUnitType.OFFER)) {
            shopUnit.setChildren(null);
        } else {
            for (var child : productRepository.findByParentId(shopUnit.getId())) {
                getNullChildrenForOffer(child);
            }
        }
    }
}
