package com.secondspin.product.controller;

import com.secondspin.product.service.IFavoritesService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private final IFavoritesService favoritesService;

    public FavoritesController(IFavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }


}
