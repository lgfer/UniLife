package com.unilife.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unilife.entity.Coupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 */
public interface CouponMapper extends BaseMapper<Coupon> {

    List<Coupon> queryCouponOfMerchant(@Param("merchantId") Long merchantId);
}
