package com.secondspin.product.controller;

import com.secondspin.product.pojo.Categories;
import com.secondspin.product.service.ICategoriesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoriesController {

    private final ICategoriesService categoriesService;

    public CategoriesController(ICategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @GetMapping
    public List<Categories> getCategories() {
        return categoriesService.getAll();
    }
}
