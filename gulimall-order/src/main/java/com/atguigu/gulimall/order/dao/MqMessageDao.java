package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.MqMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author lzp
 * @email lzp@gmail.com
 * @date 2022-03-09 11:18:32
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {
	
}
