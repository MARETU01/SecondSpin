package com.secondspin.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.common.utils.Result;
import com.secondspin.product.dto.FavoriteProductListDTO;
import com.secondspin.product.pojo.Favorites;
import com.secondspin.product.service.IFavoritesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private final IFavoritesService favoritesService;
    private final ObjectMapper jacksonObjectMapper;

    public FavoritesController(IFavoritesService favoritesService, ObjectMapper jacksonObjectMapper) {
        this.favoritesService = favoritesService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @GetMapping
    public Result<PageDTO<FavoriteProductListDTO>> getFavorites(@RequestHeader("user-info") String userJson,
                                                                QueryDTO queryDTO) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(favoritesService.getFavorites(user.getUserId(), queryDTO));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @DeleteMapping
    public Result<Boolean> removeFavoritesByIds(@RequestHeader("user-info") String userJson,
                                                @RequestParam("ids") List<Integer> ids) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(favoritesService.lambdaUpdate()
                    .eq(Favorites::getUserId, user.getUserId())
                    .in(Favorites::getProductId, ids)
                    .remove()
            );
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
