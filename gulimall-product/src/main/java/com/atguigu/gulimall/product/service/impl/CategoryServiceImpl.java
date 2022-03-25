package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
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

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys........");
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(
                new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间："+ (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询了数据库");

        //将数据库的多次查询变为一次
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //1、查出所有分类
        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        return parentCid;
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

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        // return this.baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
    }

}