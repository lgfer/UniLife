package com.unilife.service;

import com.unilife.dto.Result;
import com.unilife.entity.MerchantType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IMerchantTypeService extends IService<MerchantType> {

    Result querySort();
}
