package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.RabbitInfo;
import com.atguigu.common.enume.OrderStatusEnum;
import com.atguigu.common.exception.NotStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Slf4j
@Service("wareSkuService")
//@RabbitListener(queues = RabbitInfo.Stock.releaseQueue)
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;

    @Resource
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId) && !"0".equalsIgnoreCase(skuId)) {
            queryWrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId) && !"0".equalsIgnoreCase(wareId)) {
            queryWrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        //1、判读如果没有这个库存记录新增
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));

        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败整个事务无需回滚
            //1、自己catch异常
            try{
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            //添加库存信息
            wareSkuDao.insert(wareSkuEntity);
        } else {
            //修改库存信息
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(item -> {
            //所有库的库存减去被锁定的库存
            Long count = this.baseMapper.getSkuStock(item);
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(item);
            skuHasStockVo.setHasStock(count == null?false:count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return skuHasStockVos;
    }

    /**
     * 锁库存
     * 解锁库存场景
     * 下订单成功，订单过期没有支付被系统自动取消，或者被用户手动取消，需要解锁库存
     * 下订单成功，库存锁定成功，接下来的业务调用失败，需要解锁库存
     *
     * @param vo
     * @return
     */

    @Transactional(rollbackFor = NotStockException.class) // 自定义异常
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) { // List<LockStockResult>

        // 锁库存之前先保存订单 以便后来消息撤回
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);
        // [理论上]1. 按照下单的收获地址 找到一个就近仓库, 锁定库存
        // [实际上]1. 找到每一个商品在哪一个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();//订单项
        List<SkuWareHasStock> lockVOs = locks.stream().map(item -> {
            // 创建订单项
            SkuWareHasStock hasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            hasStock.setSkuId(skuId);
            // 查询本商品在哪有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            hasStock.setWareId(wareIds);
            hasStock.setNum(item.getCount());//购买数量
            return hasStock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock hasStock : lockVOs) {
            Boolean skuStocked = true;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个库存（注意可能会回滚之前的订单项，没关系）
                throw new NotStockException(skuId.toString());
            }
            // 1 如果每一个商品都锁定成功 将当前商品锁定了几件的工作单记录发送给MQ
            // 2 如果锁定失败 前面保存的工作单信息回滚了(发送了消息却回滚库存的情况，没关系，用数据库id查就可以)
            //此处代码有问题，会所有仓库该产品都锁库存。例如，如果两个仓库，都有，那么这俩仓库都锁了一个库存，导致最终锁的是两个库存。
            for (Long wareId : wareIds) {
                // 成功就返回 1 失败返回0  （有上下限）
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());//要锁定num个
                // UPDATE返回0代表失败
                if (count == 0) { // 没有更新对，说明锁当前库库存失败，去尝试其他库
                    skuStocked = false;
                } else { // 即1
                    // TODO 告诉MQ库存锁定成功 一个订单锁定成功 消息队列就会有一个消息
                    // 订单项详情
                    WareOrderTaskDetailEntity detailEntity =
                            new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(),
                                    wareId, // 锁定的仓库号
                                    1);
                    // db保存订单sku项工作单详情，告诉商品锁的哪个库存
                    orderTaskDetailService.save(detailEntity);
                    // 发送库存锁定消息到延迟队列
                    // 要发送的内容
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, detailTo);
                    // 如果只发详情id，那么如果出现异常数据库回滚了 // 这个地方需要斟酌，都在事务里了，其实没必要
                    stockLockedTo.setDetailTo(detailTo);

                    rabbitTemplate.convertAndSend(RabbitInfo.Stock.exchange,
                            RabbitInfo.Stock.delayRoutingKey, stockLockedTo);
                    skuStocked = true;
                    break;// 一定要跳出，防止重复发送多余消息
                }

            }
            if (!skuStocked) {
                // 当前商品在所有仓库都没锁柱
                throw new NotStockException(skuId.toString());
            }
        }
        // 3.全部锁定成功
        return true;
    }

    /**
     * 防止订单服务卡顿 导致订单状态一直改不了 库存消息有限到期 最后导致卡顿的订单 永远无法解锁库存
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo to) {
        log.info("\n订单超时自动关闭,准备解锁库存");
        String orderSn = to.getOrderSn();
        // 查一下最新的库存状态 防止重复解锁库存[Order服务可能会提前解锁]
        WareOrderTaskEntity taskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskEntityId = taskEntity.getId();
        // 按照工作单找到所有 没有解锁的库存 进行解锁 状态为1等于已锁定
        List<WareOrderTaskDetailEntity> entities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskEntityId).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unLock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    /**
     * 流程图：![](https://i0.hdslb.com/bfs/album/cf307afd8fc216266719f5f6512d62379c183335.png)
     * 解锁库存
     * 	查询数据库关系这个订单的详情
     * 		有: 证明库存锁定成功
     * 			1.没有这个订单, 必须解锁
     * 			2.有这个订单 不是解锁库存
     * 				订单状态：已取消,解锁库存
     * 				没取消：不能解锁	;
     * 		没有：就是库存锁定失败， 库存回滚了 这种情况无需回滚
     */
    @Override
    //@RabbitHandler
    public void unlockStock(StockLockedTo to) {
        log.info("\n收到解锁库存的消息");
        // 库存id
        Long id = to.getId();
        StockDetailTo detailTo = to.getDetailTo();
        Long detailId = detailTo.getId();

        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            // 解锁
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // 根据订单号 查询订单状态 已取消才解锁库存
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            /**   */
            if (orderStatus.getCode() == 0) {
                // 订单数据返回成功
                OrderVo orderVo = orderStatus.getData(new TypeReference<OrderVo>() {
                });
                // 订单不存在或订单已取消
                if (orderVo == null || orderVo.getStatus() == OrderStatusEnum.CANCLED.getCode()) {
                    // 订单已取消 状态1 已锁定  这样才可以解锁
                    if (byId.getLockStatus() == 1) {
                        unLock(detailTo.getSkuId(), detailTo.getWareId(), detailTo.getSkuNum(), detailId);
                    }
                }
            } else {
                // 消息拒绝 重新放回队列 让别人继续消费解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            // 无需解锁
        }
    }

    /**
     * 解锁库存
     */
    private void unLock(Long skuId, Long wareId, Integer num, Long taskDeailId) {
        // 更新库存
        wareSkuDao.unlockStock(skuId, wareId, num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(taskDeailId);
        detailEntity.setLockStatus(2);
        orderTaskDetailService.updateById(detailEntity);
    }

    @Data
    class SkuWareHasStock {

        private Long skuId;

        private List<Long> wareId;

        private Integer num;
    }

}