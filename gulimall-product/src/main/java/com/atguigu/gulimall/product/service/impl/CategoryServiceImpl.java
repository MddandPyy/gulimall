package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    //本地缓存
    //private Map<String,Object> cache = new HashMap<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

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

        //1、加入缓存的数据都是存成json字符串
        //json跨语言，跨平台兼容
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(StringUtils.isEmpty(catalogJSON)){
            //缓存中没有，查数据库，并将结果放置缓存中
            //为了放置缓存穿透，可以在此处加锁。当判断缓存中没有时，加分布式锁，只有一个请求可以去数据库查询，并将结果赋值到缓存中，其他请求从缓存中获取。
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();
            return catalogJsonFromDB;
        }
        System.out.println("命中缓存，直接返回。。。。");
        Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        return stringListMap;
    }


    /**
     * 从数据库查询并封装数据::分布式锁，使用redisson
     * 缓存里的数据如何和数据库的数据保持一致？？
     * 缓存数据一致性
     * 1)、双写模式  改完数据库，改缓存。更新关联缓存太麻烦。
     *     双写一致性，改数据和改缓存不是原子操作。需要保证修改数据库的顺序要和修改缓存的顺序一致。才会保证数据一致。
     * 2)、失效模式  改完数据库，删掉缓存。重新缓存
     *     失效模式也有问题，如果写操作和删除也不是原子操作。在删除缓存后，第一次访问需要读取数据，并放入缓存。加入读取数据后，
     *     数据被其他线程修改，并执行了删缓存操作，然后本线程才继续执行放入缓存的操作。这就导致了缓存中是上一个修改的内容。
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        //1、获取一把锁，只要锁的名字一样，就是同一个把锁,redis中锁的key为my-lock。默认过期时间30S，看门狗机制，会自动续期
        //（锁的粒度，越细越快:具体缓存的是某个数据，11号商品） product-11-lock
        RLock lock = redissonClient.getLock("my-lock");
        //加锁不成功，会阻塞式等待。因此需要像下面手写redis方法中那样，进行递归调用循环获取锁
        lock.lock();
        //lock.lock();
        Map<String, List<Catelog2Vo>> catalogJsonFromDB ;
        try {
            //可重入，同一线程多次进入，redis中value+1;
            catalogJsonFromDB  = getCatalogJsonFromDB();
        }finally {
            lock.unlock();
        //    lock.unlock();
        }
        return catalogJsonFromDB;

    }




        /**
         * 从数据库查询并封装数据::分布式锁
         * @return
         */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {

        /**
         * 抢占锁后，代码异常或者机器宕机，导致删除锁代码（宕机，放在finally中也没用）没有执行，
         * 1、设置过期时间
         * 2、删除锁的时候，判断只能删除自己的锁（过期时间太短，业务没处理完，就过期，被其他线程占住。删除时判断是否是自己加的那把锁）
         */
        System.out.println("没有命中缓存，想要获取分布式锁，并查询数据库");
        String uuid = UUID.randomUUID().toString();
        //1、占分布式锁，去redis占坑，设置过期时间。加锁和设置过期时间需要原子操作.过期时间需要合理，以及考虑到锁的续期问题
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,300, TimeUnit.SECONDS);
        if(lock){
            System.out.println("获取分布式锁成功");
            //2、获取数据并解锁
            Map<String, List<Catelog2Vo>> catalogJsonFromDB ;
            try{
                catalogJsonFromDB  = getCatalogJsonFromDB();
            }

//            String lockValue = redisTemplate.opsForValue().get("lock");
//            //删除时判断是否是自己加的那把锁
//            if(uuid.equals(lockValue)){
//                //查redis判断逻辑的时候，是自己的锁，但是在执行删除之前，失效被其他线程占用。就是此注释位置期间锁被改变
//                redisTemplate.delete("lock");
//            }

            finally {
                //判断是否是自己的锁，和删除锁也需要是原子操作， lua脚本解锁，判断和删除用一句语句执行
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return catalogJsonFromDB;
        }else{
            System.out.println("获取分布式锁失败，等待重试");
            return getCatalogJsonFromDBWithRedisLock();
        }
    }



    /**
     * 从数据库查询并封装数据::本地锁
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        System.out.println("没有命中缓存，想要获取本地锁，并查询数据库");
        synchronized (this) {
            return getCatalogJsonFromDB();
        }
    }


    /**
     * 每次都从数据库获取，使用缓存优化
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDB() {

        //json跨语言，跨平台兼容
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(!StringUtils.isEmpty(catalogJSON)){
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        }else{
            System.out.println("查询了数据库。。。。。。");

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
                //cache.put("test",catelog2Vos);

                return catelog2Vos;
            }));
            String s = JSON.toJSONString(parentCid);
            redisTemplate.opsForValue().set("catalogJSON",s);
            return parentCid;
        }

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