package com.timotege;

import com.timotege.dao.model.ShopUnitImport;
import com.timotege.dao.model.ShopUnitImportRequest;
import com.timotege.dao.model.ShopUnitType;
import com.timotege.dao.repository.HistoryRepository;
import com.timotege.dao.repository.ProductRepository;
import com.timotege.exception.InvalidDataException;
import com.timotege.service.ProductServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Test
    public void getUnit() {
        assert 1 == 1;
    }

	/*@Test
	public void importTest() {
		List<ShopUnitImport> items = new ArrayList<>();
		ShopUnitImport toyota= new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.CATEGORY, null);
		ShopUnitImport prius = new ShopUnitImport(UUID.randomUUID(), toyota.getId(), "prius", ShopUnitType.OFFER, null);
		ShopUnitImport camry = new ShopUnitImport(UUID.randomUUID(), toyota.getId(), "camry", ShopUnitType.OFFER, 200);
		items.add(toyota);
		items.add(camry);
		items.add(prius);
		ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-04-27T23:29:01.001Z");
		productService.importProducts(shopUnitImportRequest);
	}*/

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
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, "2022-13-32T25:25:61.001Z");
        productService.importProducts(shopUnitImportRequest);
    }


}
