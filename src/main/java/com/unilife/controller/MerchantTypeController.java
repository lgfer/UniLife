package com.unilife.controller;


import com.unilife.dto.Result;
import com.unilife.entity.MerchantType;
import com.unilife.service.IMerchantTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 */
@RestController
@RequestMapping("/merchant-type")
public class MerchantTypeController {
    @Resource
    private IMerchantTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
//        List<MerchantType> typeList = typeService
//                .query().orderByAsc("sort").list();

        return typeService.querySort();
    }
}
