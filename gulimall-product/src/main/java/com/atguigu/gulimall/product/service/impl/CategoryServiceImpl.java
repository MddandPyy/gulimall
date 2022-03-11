package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //baseMapper就是categoryDao
        List<CategoryEntity> list = baseMapper.selectList(null);
        List<CategoryEntity> level1 = list.stream().filter(e -> e.getParentCid() == 0).map((e)->{
            e.setChildren(getChildren(e,list));
            return e;
        }).sorted((e1,e2)-> e1.getSort()- e2.getSort()).collect(Collectors.toList());

        return level1;
    }

    //使用递归查找所有菜单子菜单
    private  List<CategoryEntity> getChildren(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter((e) -> e.getParentCid() == root.getCatId()).map(e -> {
            //找到子菜单
            e.setChildren(getChildren(e, all));
            return e;
        }).sorted((e1, e2) -> e1.getSort() - e2.getSort()).collect(Collectors.toList());
        return children;
    }

}