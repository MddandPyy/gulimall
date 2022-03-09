package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author lzp
 * @email lzp@gmail.com
 * @date 2022-03-09 10:47:06
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
