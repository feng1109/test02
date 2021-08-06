package com.eseasky.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author YINJUN
 * @Date 2021年08月05日 15:49
 * @Description:物联设备controller层
 */
@Api(value = "物联设备controller层",tags = "物联设备controller层")
@RestController
@RequestMapping("/iotp/equipment")
public class IotpEquipmentController {

    @GetMapping("test")
    public String test(){
        return "success";
    }
}
