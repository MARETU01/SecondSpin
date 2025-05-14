package com.secondspin.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.common.utils.Result;
import com.secondspin.user.pojo.Address;
import com.secondspin.user.pojo.Users;
import com.secondspin.user.service.IAddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    private final IAddressService addressService;
    private final ObjectMapper jacksonObjectMapper;

    public AddressController(IAddressService addressService, ObjectMapper jacksonObjectMapper) {
        this.addressService = addressService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @GetMapping
    public Result<List<Address>> getAddress(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        try {
            return Result.success(addressService.getAddressByUserId(user.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> addAddress(@RequestHeader("user-info") String userJson, @RequestBody Address address) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        try {
            return Result.success(addressService.saveAddress(user, address));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping
    public Result<Boolean> updateAddress(@RequestHeader("user-info") String userJson, @RequestBody Address address) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        try {
            return Result.success(addressService.updateAddress(user, address));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @DeleteMapping
    public Result<Boolean> deleteAddress(@RequestHeader("user-info") String userJson, @RequestBody List<Long> ids) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        try {
            return Result.success(addressService.deleteAddresses(user, ids));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Address getAddress(@PathVariable("id") Long id) {
        return addressService.getAddressById(id);
    }
}
