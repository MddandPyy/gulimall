package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 分布式基础篇
     * -- Category三级联动树组件
     * -- lambda 表达式中
     * -- filter 里判断当前遍历到的元素 parentCid 是否等于当前元素的 Cid 时
     * -- 需要将 == 改为 equals()
     *
     * 因为 cid 是Long 类型，在超出 【-128, 127】 的区间时，会new出新的对象，此时使用 == 出现 fasle
     * @return
     */
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

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //TODO 1、删除校验，删除的菜单，是否在被别的地方被引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public List<CategoryEntity> listWithTreeById(Long id) {
        List<CategoryEntity> list = baseMapper.selectList(null);
        List<CategoryEntity> level1 = list.stream().filter(e -> e.getCatId().equals(id)).map((e)->{
            e.setChildren(getChildren(e,list));
            return e;
        }).sorted((e1,e2)-> e1.getSort()- e2.getSort()).collect(Collectors.toList());

        return level1;
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        //递归有先放数据，和后方数据的问题
        //List<Long> paths = new ArrayList<>();
        //List<Long> longs = fineParentPath(catelogId, paths);
        //Collections.reverse(longs);



        List<Long> longs = new ArrayList<>();
        //fineParentPath2(catelogId, longs);
        //Collections.reverse(longs);
        fineParentPath(catelogId, longs);
        return longs.toArray(new Long[longs.size()]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());

    }

    private void fineParentPath(long catelogId,List<Long> paths){
        CategoryEntity entity = this.getById(catelogId);
        if(entity.getParentCid()!=0){
            fineParentPath(entity.getParentCid(),paths);
            paths.add(entity.getCatId());
        }else{
            paths.add(entity.getCatId());
        }

    }

//    private List<Long> fineParentPath(long catelogId,List<Long> paths){
//        paths.add(catelogId);
//        CategoryEntity entity = this.getById(catelogId);
//        if(entity.getParentCid()!=0){
//            fineParentPath(entity.getParentCid(),paths);
//        }
//        return paths;
//
//    }

//    private void fineParentPath2(long catelogId,List<Long> paths){
//        paths.add(catelogId);
//        CategoryEntity entity = this.getById(catelogId);
//        if(entity.getParentCid()!=0){
//            fineParentPath2(entity.getParentCid(),paths);
//        }
//    }

    //使用递归查找所有菜单子菜单
    private  List<CategoryEntity> getChildren(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter((e) -> e.getParentCid().equals(root.getCatId())).map(e -> {
            //找到子菜单
            e.setChildren(getChildren(e, all));
            return e;
        }).sorted((e1, e2) -> e1.getSort() - e2.getSort()).collect(Collectors.toList());
        return children;
    }

}