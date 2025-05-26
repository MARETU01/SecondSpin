package com.secondspin.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.common.utils.Result;
import com.secondspin.product.dto.ProductListDTO;
import com.secondspin.product.dto.ViewHistoryDTO;
import com.secondspin.product.pojo.ViewHistory;
import com.secondspin.product.service.IViewHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
public class ViewHistoryController {

    private final IViewHistoryService viewHistoryService;
    private final ObjectMapper jacksonObjectMapper;

    public ViewHistoryController(IViewHistoryService viewHistoryService, ObjectMapper jacksonObjectMapper) {
        this.viewHistoryService = viewHistoryService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @GetMapping
    public Result<PageDTO<ViewHistoryDTO>> getViewHistory(@RequestHeader("user-info") String userJson,
                                                          QueryDTO queryDTO) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(viewHistoryService.getViewHistory(user.getUserId(), queryDTO));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @DeleteMapping("/all")
    public Result<Boolean> clearViewHistory(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(viewHistoryService.lambdaUpdate().eq(ViewHistory::getUserId, user.getUserId()).remove());
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @DeleteMapping
    public Result<Boolean> removeViewHistoryByIds(@RequestHeader("user-info") String userJson,
                                              @RequestParam("ids") List<Integer> ids) throws JsonProcessingException {
        JwtUser user = jacksonObjectMapper.readValue(userJson, JwtUser.class);
        try {
            return Result.success(viewHistoryService.removeBatchByIds(ids));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
