package com.secondspin.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondspin.user.pojo.Address;
import com.secondspin.user.pojo.Users;
import com.secondspin.user.service.IAddressService;
import lombok.SneakyThrows;
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
    public List<Address> getAddress(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        return addressService.getAddressByUserId(user.getUserId());
    }

    @PostMapping
    public Boolean addAddress(@RequestHeader("user-info") String userJson, @RequestBody Address address) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        return addressService.saveAddress(user, address);
    }

    @PutMapping
    public Boolean updateAddress(@RequestHeader("user-info") String userJson, @RequestBody Address address) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        return addressService.updateAddress(user, address);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteAddress(@RequestHeader("user-info") String userJson, @PathVariable("id") Long id) throws JsonProcessingException {
        Users user = jacksonObjectMapper.readValue(userJson, Users.class);
        return addressService.deleteAddress(user, id);
    }
}
