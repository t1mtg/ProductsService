package com.timotege;

import com.timotege.dao.model.*;
import com.timotege.dao.repository.HistoryRepository;
import com.timotege.dao.repository.ProductRepository;
import com.timotege.exception.InvalidDataException;
import com.timotege.exception.ItemNotFoundException;
import com.timotege.service.ProductServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ProductServiceApplicationTests {
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductServiceImpl productService;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void contextLoads() {
    }

    @Test(expected = InvalidDataException.class)
    public void nullOfferPriceThrowsInvalidDataException() {
        List<ShopUnitImport> items = new ArrayList<>();
        ShopUnitImport prius = new ShopUnitImport(UUID.randomUUID(), null, "prius", ShopUnitType.OFFER, null);
        items.add(prius);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-04-27T23:29:01.001Z");
        productService.importProducts(shopUnitImportRequest);
    }

    @Test(expected = InvalidDataException.class)
    public void incorrectDateThrowsInvalidDataException() {
        List<ShopUnitImport> items = new ArrayList<>();
        ShopUnitImport prius = new ShopUnitImport(UUID.randomUUID(), null, "prius", ShopUnitType.OFFER, 32);
        items.add(prius);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-13-32T25:25:61.001Z");
        productService.importProducts(shopUnitImportRequest);
    }

    @Test(expected = InvalidDataException.class)
    public void notNullCategoryPriceThrowsInvalidDataException() {
        List<ShopUnitImport> items = new ArrayList<>();
        ShopUnitImport prius = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.CATEGORY, 23);
        items.add(prius);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-05-30T23:23:02.001Z");
        productService.importProducts(shopUnitImportRequest);
    }


    @Test(expected = InvalidDataException.class)
    public void nullIdThrowsInvalidDataException() {
        List<ShopUnitImport> items = new ArrayList<>();
        ShopUnitImport prius = new ShopUnitImport(null, null, "toyota", ShopUnitType.OFFER, 23);
        items.add(prius);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-11-11T21:11:11.001Z");
        productService.importProducts(shopUnitImportRequest);
    }

    @Test(expected = InvalidDataException.class)
    public void nullTypeThrowsInvalidDataException() {
        List<ShopUnitImport> items = new ArrayList<>();
        ShopUnitImport prius = new ShopUnitImport(UUID.randomUUID(), null, "toyota", null, 23);
        items.add(prius);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-11-11T21:11:11.001Z");
        productService.importProducts(shopUnitImportRequest);
    }

    @Test(expected = InvalidDataException.class)
    public void nullNameThrowsInvalidDataException() {
        List<ShopUnitImport> items = new ArrayList<>();
        ShopUnitImport prius = new ShopUnitImport(UUID.randomUUID(), null, null, ShopUnitType.OFFER, 23);
        items.add(prius);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-11-11T21:11:11.001Z");
        productService.importProducts(shopUnitImportRequest);
    }

    @Test(expected = InvalidDataException.class)
    public void offerIsParentThrowsInvalidDataException() {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.OFFER, 1234);
        ShopUnitImport prius = new ShopUnitImport(UUID.randomUUID(), toyota.getId(), "prius", ShopUnitType.OFFER, 100);
        ShopUnitImport camry = new ShopUnitImport(UUID.randomUUID(), toyota.getId(), "camry", ShopUnitType.OFFER, 200);
        List<ShopUnitImport> items = Arrays.asList(toyota, prius, camry);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-11-11T21:11:11.001Z");
        when(productRepository.findById(any())).thenReturn(Optional.of(new ShopUnit(
                toyota.getId(),
                null,
                toyota.getName(),
                LocalDateTime.now(),
                toyota.getType(),
                toyota.getPrice()
        )));
        productService.importProducts(shopUnitImportRequest);
    }


    @Test
    public void categoryPriceUpdated() {
        ShopUnit toyotaUnit = new ShopUnit(UUID.randomUUID(), null, "toyota", LocalDateTime.now(), ShopUnitType.CATEGORY, null);
        ShopUnit priusUnit = new ShopUnit(UUID.randomUUID(), toyotaUnit.getId(), "prius", LocalDateTime.now(), ShopUnitType.OFFER, 100);
        ShopUnit camryUnit = new ShopUnit(UUID.randomUUID(), toyotaUnit.getId(), "camry", LocalDateTime.now(), ShopUnitType.OFFER, 200);
        when(productRepository.findByParentId(any())).thenReturn(Arrays.asList(camryUnit, priusUnit));
        productService.updateCategoryPrice(toyotaUnit, LocalDateTime.now());
        assertEquals(toyotaUnit.getPrice().intValue(), 150);
    }

    @Test
    public void getShopUnit_UnitFound() {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.OFFER, 1234);
        when(productRepository.findById(toyota.getId())).thenReturn(Optional.of(new ShopUnit(
                toyota.getId(),
                null,
                toyota.getName(),
                LocalDateTime.now(),
                toyota.getType(),
                toyota.getPrice()
        )));
        var res = productService.getShopUnit(toyota.getId().toString());
        assertEquals(res.getId(), toyota.getId());
    }

    @Test(expected = ItemNotFoundException.class)
    public void getShopUnit_UnitNotFound() {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.OFFER, 1234);
        when(productRepository.findById(toyota.getId())).thenReturn(Optional.empty());
        var res = productService.getShopUnit(toyota.getId().toString());
        assertEquals(res.getId(), toyota.getId());
    }

    @Test(expected = InvalidDataException.class)
    public void getShopUnit_IncorrectId() {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.OFFER, 1234);
        var res = productService.getShopUnit("Yes, indeed.");
        assertEquals(res.getId(), toyota.getId());
    }

    @Test()
    public void deleteUnit_UnitDeleted_ReturnedStatus404() {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.OFFER, 1234);
        ShopUnit toyotaUnit = new ShopUnit(UUID.randomUUID(), null, "toyota", LocalDateTime.now(), ShopUnitType.CATEGORY, null);
        when(productRepository.findById(any())).thenReturn(Optional.of(toyotaUnit)).thenReturn(Optional.empty());
        productService.deleteProduct(toyota.getId().toString());
    }

    @Test(expected = ItemNotFoundException.class)
    public void deleteNonExistentUnit_ReturnedStatus404() {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.OFFER, 1234);
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        productService.deleteProduct(toyota.getId().toString());
    }

    @Test()
    public void getUnitsWithPriceChanged_UnitsReturned() {
        ShopUnit toyotaUnit = new ShopUnit(UUID.randomUUID(), null, "toyota", LocalDateTime.now(), ShopUnitType.CATEGORY, null);
        ShopUnit mitsubishiUnit = new ShopUnit(UUID.randomUUID(), null, "mitsubishi", LocalDateTime.now(), ShopUnitType.CATEGORY, null);
        ShopUnit renaultUnit = new ShopUnit(UUID.randomUUID(), null, "renault", LocalDateTime.now(), ShopUnitType.CATEGORY, null);
        when(productRepository.findByDateIsBetween(any(), any())).thenReturn(List.of(toyotaUnit, mitsubishiUnit, renaultUnit));
        var res = productService.getUnitsWithPriceChanged("2022-11-11T21:11:11.001Z");
        assertEquals(res.getItems().size(), 3);
    }

    @Test()
    public void getStatistics_UnitsReturned() {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.OFFER, 1234);
        ShopUnitStatisticUnit toyotaUnit = new ShopUnitStatisticUnit(
                toyota.getId(),
                null,
                "toyota",
                ShopUnitType.CATEGORY,
                null,
                LocalDateTime.now());
        when(historyRepository.findByDateTimeGreaterThanEqualAndDateTimeLessThanAndUnitId(any(), any(), any()))
                .thenReturn(List.of(toyotaUnit));
        var res = productService.getStatistics(
                toyota.getId().toString(),
                "2022-11-12T21:11:11.001Z",
                "2022-11-13T21:11:11.001Z");
        assertEquals(res.getItems().get(0).getId(), toyota.getId());
    }


}

