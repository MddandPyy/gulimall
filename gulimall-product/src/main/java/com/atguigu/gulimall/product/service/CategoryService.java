package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author lzp
 * @email lzp@gmail.com
 * @date 2022-03-09 09:38:07
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenusByIds(List<Long> asList);

    List<CategoryEntity> listWithTreeById(Long id);

    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);
}

