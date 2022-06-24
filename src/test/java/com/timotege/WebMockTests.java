package com.timotege;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timotege.controller.ProductController;
import com.timotege.dao.model.*;
import com.timotege.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc
public class WebMockTests {
    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void updateProducts_ReturnedStatus200() throws Exception {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.CATEGORY, null);
        ShopUnitImport prius = new ShopUnitImport(UUID.randomUUID(), toyota.getId(), "prius", ShopUnitType.OFFER, 100);
        ShopUnitImport camry = new ShopUnitImport(UUID.randomUUID(), toyota.getId(), "camry", ShopUnitType.OFFER, 200);
        List<ShopUnitImport> items = Arrays.asList(toyota, prius, camry);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, LocalDateTime.now().toString());
        this.mockMvc.perform((post("/imports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(shopUnitImportRequest))))
                .andExpect(status().isOk());
    }


    @Test
    public void updateProductsValidData_MapsTuBusinessLogic() throws Exception {
        ShopUnitImport toyota = new ShopUnitImport(UUID.randomUUID(), null, "toyota", ShopUnitType.CATEGORY, null);
        List<ShopUnitImport> items = Arrays.asList(toyota);
        ShopUnitImportRequest shopUnitImportRequest = new ShopUnitImportRequest(items, LocalDateTime.now().toString());
        this.mockMvc.perform((post("/imports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(shopUnitImportRequest))))
                .andExpect(status().isOk());

        ArgumentCaptor<ShopUnitImportRequest> userCaptor = ArgumentCaptor.forClass(ShopUnitImportRequest.class);
        verify(productService, times(1)).importProducts(userCaptor.capture());
        assert(userCaptor.getValue().getItems().stream().findFirst().get().getId().equals(toyota.getId()));
    }


    @Test
    public void getUnit_UnitReturned() throws Exception {
        UUID id = UUID.fromString("1cb8fa74-dfa4-4362-9f91-78673785bc8d");
        ShopUnit camry = new ShopUnit(id, null, "camry", LocalDateTime.now(), ShopUnitType.OFFER, 1000);
        ShopUnit lancer = new ShopUnit(UUID.randomUUID(), null, "camry", LocalDateTime.now(), ShopUnitType.OFFER, 1000);
        Mockito.when(productService.getShopUnit(any())).thenReturn(camry);
        this.mockMvc.perform(get("/nodes/1cb8fa74-dfa4-4362-9f91-78673785bc8d")).andExpect(status().isOk())
                .andExpect(content().string(containsString(camry.getId().toString())));
    }

    @Test
    public void deleteUnit_UnitDeleted() throws Exception {
        UUID id = UUID.fromString("1cb8fa74-dfa4-4362-9f91-78673785bc8d");
        ShopUnit camry = new ShopUnit(id, null, "camry", LocalDateTime.now(), ShopUnitType.OFFER, 1000);
        ShopUnit lancer = new ShopUnit(UUID.randomUUID(), null, "camry", LocalDateTime.now(), ShopUnitType.OFFER, 1000);
        Mockito.when(productService.getShopUnit(any())).thenReturn(camry).thenReturn(null);
        this.mockMvc.perform(delete("/delete/1cb8fa74-dfa4-4362-9f91-78673785bc8d")).andExpect(status().isOk());
    }

    @Test
    public void getUnitsWithPriceChanged_UnitsGot() throws Exception {
        UUID id = UUID.fromString("1cb8fa74-dfa4-4362-9f91-78673785bc8d");
        ShopUnitStatisticUnit camry = new ShopUnitStatisticUnit(id, null, "camry", ShopUnitType.OFFER, 1000, LocalDateTime.now());
        ShopUnitStatisticUnit lancer = new ShopUnitStatisticUnit(UUID.randomUUID(), null, "camry", ShopUnitType.OFFER, 1000, LocalDateTime.now());
        Mockito.when(productService.getUnitsWithPriceChanged(any())).thenReturn(new ShopUnitStatisticResponse(Arrays.asList(camry, lancer)));
        this.mockMvc.perform(get("/sales/")
                .param("date", String.valueOf(LocalDateTime.now().plusHours(1))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(camry.getId().toString())))
                .andExpect(content().string(containsString(lancer.getId().toString())));
    }

    @Test
    public void getUnitsChangedWithinPeriod_UnitsGot() throws Exception {
        UUID id = UUID.fromString("1cb8fa74-dfa4-4362-9f91-78673785bc8d");
        ShopUnitStatisticUnit camry = new ShopUnitStatisticUnit(id, null, "camry", ShopUnitType.OFFER, 1000, LocalDateTime.now());
        Mockito.when(productService.getStatistics(any(), any(), any())).thenReturn(new ShopUnitStatisticResponse(List.of(camry)));
        this.mockMvc.perform(get("/node/1cb8fa74-dfa4-4362-9f91-78673785bc8d/statistic")
                        .param("dateStart", String.valueOf(LocalDateTime.now().plusHours(1)))
                        .param("dateEnd",  String.valueOf(LocalDateTime.now().plusHours(1))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(camry.getId().toString())));
    }

}
