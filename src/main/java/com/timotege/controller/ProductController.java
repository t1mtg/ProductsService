package com.timotege.controller;

import com.timotege.dao.model.Error;
import com.timotege.dao.model.ShopUnit;
import com.timotege.dao.model.ShopUnitImportRequest;
import com.timotege.dao.model.ShopUnitStatisticResponse;
import com.timotege.exception.InvalidDataException;
import com.timotege.service.ProductService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/imports")
    public HttpStatus importProducts(@RequestBody ShopUnitImportRequest shopUnitImportRequest) {
        productService.importProducts(shopUnitImportRequest);
        return HttpStatus.OK;
    }

    @GetMapping("/nodes/{id}")
    public ShopUnit getShopUnit(@PathVariable String id) {
        return productService.getShopUnit(id);
    }

    @DeleteMapping("/delete/{id}")
    public HttpStatus deleteProductById(@PathVariable String id) {
        productService.deleteProduct(id);
        return HttpStatus.OK;
    }

    @GetMapping("/sales/{date}")
    public ShopUnitStatisticResponse getUnitsWithPriceChanged(@PathVariable String date) {
        return productService.getUnitsWithPriceChanged(date);
    }

    @GetMapping("/node/{id}/statistic")
    public ShopUnitStatisticResponse getStatistics(@PathVariable String id,
                                                   @RequestParam(value = "start") String dateStart,
                                                   @RequestParam(value = "end") String dateFinish) {
        return productService.getStatistics(id, dateStart, dateFinish);
    }



    @ExceptionHandler(InvalidDataException.class)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleWrongInput() {
        return new Error(400, "Validation Failed");
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error unitNotFound() {
        return new Error(404, "Item not found");
    }
}
