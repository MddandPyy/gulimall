package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author lzp
 * @email lzp@gmail.com
 * @date 2022-03-09 09:38:07
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
