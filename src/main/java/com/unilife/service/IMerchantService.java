package com.unilife.service;

import com.unilife.dto.Result;
import com.unilife.entity.Merchant;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IMerchantService extends IService<Merchant> {
    Result queryById(Long id) throws InterruptedException;

    Result update(Merchant merchant);

    Result queryMerchantByType(Integer typeId, Integer current, Double x, Double y);
}
