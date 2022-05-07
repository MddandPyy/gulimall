package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.RabbitInfo;
import com.atguigu.common.to.SeckillSkuRedisTo;
import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.common.vo.SkuInfoVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>Title: SeckillServiceImpl</p>
 * Description：
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

	@Autowired
	private CouponFeignService couponFeignService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private ProductFeignService productFeignService;

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	private final String SESSION_CACHE_PREFIX = "seckill:sessions:";

	private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

	private final String SKUSTOCK_SEMAPHONE = "seckill:stock:"; // +商品随机码

	@Override // SeckillServiceImpl
	public void uploadSeckillSkuLatest3Day() {
		// 1.扫描最近三天要参加秒杀的商品
		R r = couponFeignService.getLate3DaySession();
		if(r.getCode() == 0){
			List<SeckillSessionsWithSkus> sessions = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {});
			// 2.缓存活动信息
			saveSessionInfo(sessions);
			// 3.缓存活动的关联的商品信息
			saveSessionSkuInfo(sessions);
		}
	}

	@Override
	public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

		// 1.确定当前时间属于那个秒杀场次
		long time = new Date().getTime();
		// 定义一段受保护的资源
//		try (Entry entry = SphU.entry("seckillSkus")){
			Set<String> keys = stringRedisTemplate.keys(SESSION_CACHE_PREFIX + "*");
			for (String key : keys) {
				// seckill:sessions:1593993600000_1593995400000
				String replace = key.replace("seckill:sessions:", "");
				String[] split = replace.split("_");
				long start = Long.parseLong(split[0]);
				long end = Long.parseLong(split[1]);
				// 符合秒杀时间
				if(time >= start && time <= end){
					// 2.获取这个秒杀场次的所有商品信息
					List<String> range = stringRedisTemplate.opsForList().range(key, 0, 100);
					BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
					// 获取多个sku的信息
					List<String> list = hashOps.multiGet(range);
					if(list != null){
						return list.stream().map(item -> {
							SeckillSkuRedisTo redisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
//						redisTo.setRandomCode(null);
							return redisTo;
						}).collect(Collectors.toList());
					}
					// 只能同时参与一个秒杀活动
					break;
				}
			}
//		}catch (BlockException e){
//			log.warn("资源被限流：" + e.getMessage());
//		}
		return null;
	}

	@SentinelResource(value= "getSkuSeckillInfoResource",blockHandler = "testBlockHandler")
	@Override
	public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {

		// 定义一段受保护的资源
		try (Entry entry = SphU.entry("seckillSkus")){

		}catch (BlockException e){
			log.warn("资源被限流：" + e.getMessage());
		}

		BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
		Set<String> keys = hashOps.keys();
		if(keys != null && keys.size() > 0){
			// 匹配sessionId-skuId
			String regx = "\\d-" + skuId;
			for (String key : keys) {
				if(Pattern.matches(regx, key)){
					String json = hashOps.get(key);
					SeckillSkuRedisTo to = JSON.parseObject(json, SeckillSkuRedisTo.class);
					// 如果没开始秒杀，就清除随机码
					long current = new Date().getTime();
					if(current <= to.getStartTime() || current >= to.getEndTime()){
						to.setRandomCode(null);
					}
					return to;
				}
			}
		}
		return null;
	}

	public  SeckillSkuRedisTo testBlockHandler(Long skuId,BlockException b){
		log.error("触发资源的限流方法"+b.getMessage());
		return null;
	}

	/** @return 订单号/null */
	@Override
	public String kill(String killId, String key, Integer num) {

		MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

		// 1.获取当前秒杀商品的详细信息
		BoundHashOperations<String, String, String> hashOps =
				stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
		String json = hashOps.get(killId);
		if(StringUtils.isEmpty(json)){
			return null;
		}else{
			SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
			// 校验合法性
			long time = new Date().getTime();
			if(time >= redisTo.getStartTime() && time <= redisTo.getEndTime()){
				// 1.校验随机码跟商品id是否匹配
				String randomCode = redisTo.getRandomCode();
				String skuId = redisTo.getPromotionSessionId() + "-" + redisTo.getSkuId();

				if(randomCode.equals(key) && killId.equals(skuId)){
					// 2.说明数据合法
					BigDecimal limit = redisTo.getSeckillLimit();
					if(num <= limit.intValue()){
						// 3. userId+skuId在redis中标识买过商品
						String redisKey = memberRespVo.getId() + "-" + skuId;
						// 让数据自动过期
						long ttl = redisTo.getEndTime() - redisTo.getStartTime();

						Boolean aBoolean = stringRedisTemplate.opsForValue()
								.setIfAbsent(redisKey,
										num.toString(),
										ttl<0?0:ttl,
										TimeUnit.MILLISECONDS);
						if(aBoolean){
							// 占位成功 说明从来没买过
							RSemaphore semaphore = redissonClient.getSemaphore(SKUSTOCK_SEMAPHONE + randomCode);
							boolean acquire = semaphore.tryAcquire(num);
							if(acquire){
								// 秒杀成功// 快速下单 发送MQ
								/** 生成订单号，这样数据库通过消息队列保存后，订单支付页面也知道保存的id是多少 */
								String orderSn = IdWorker.getTimeId() + UUID.randomUUID().toString().replace("-","").substring(7,8);
								SecKillOrderTo orderTo = new SecKillOrderTo();
								orderTo.setOrderSn(orderSn);
								orderTo.setMemberId(memberRespVo.getId());
								orderTo.setNum(num);
								orderTo.setSkuId(redisTo.getSkuId());
								orderTo.setSeckillPrice(redisTo.getSeckillPrice());
								orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
								rabbitTemplate.convertAndSend(RabbitInfo.Order.exchange,
										RabbitInfo.SecKill.delayRoutingKey, orderTo);
								// 返回订单号
								return orderSn;
							}
						}else {
							return null;
						}
					}
				}else{
					return null;
				}
			}else{
				return null;
			}
		}
		return null;
	}

	private void saveSessionInfo(List<SeckillSessionsWithSkus> sessions){
		if(sessions != null){
			// 遍历session
			sessions.stream().forEach(session -> {
				long startTime = session.getStartTime().getTime();

				long endTime = session.getEndTime().getTime();
				String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime; // "seckill:sessions:";
				Boolean hasKey = stringRedisTemplate.hasKey(key);
				// 防止重复添加活动到redis中
				if(!hasKey){
					// 获取所有商品id // 格式：活动id-skuId
					// 遍历sku
					List<String> skus = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "-" + item.getSkuId()).collect(Collectors.toList());
					// 缓存活动信息
					stringRedisTemplate.opsForList().leftPushAll(key, skus);
				}
			});
		}
	}

	/**
	 * 活动redis
	 * seckill:sessions:开始时间_结束时间
	 * skuIds[]
	 * ====================
	 * 商品redis
	 * Map【seckill:skus:】
	 * <session-skuId,skuId的图片、随机码等详细信息>
	 * ========================
	 * 信号量
	 * <seckill:stock:随机码,
	 * 信号量>
	 * */
	private void saveSessionSkuInfo(List<SeckillSessionsWithSkus> sessions){
		if(sessions != null){
			// 遍历session
			sessions.stream().forEach(session -> {
				// sku信息redis，准备往里放内容
				BoundHashOperations<String, Object, Object> ops =
						stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX); // "seckill:skus:";
				// 遍历sku往上面redis里放内容
				session.getRelationSkus().stream().forEach(seckillSkuVo -> {
					// 1.商品的随机码
					String randomCode = UUID.randomUUID().toString().replace("-", "");
					// 缓存中没有再添加 // key:sessionid-skuId
					if(!ops.hasKey(seckillSkuVo.getPromotionSessionId() + "-" + seckillSkuVo.getSkuId())){
						// 2.缓存商品
						SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
						BeanUtils.copyProperties(seckillSkuVo, redisTo);
						// 3.sku的基本数据 sku的秒杀信息
						R info = productFeignService.skuInfo(seckillSkuVo.getSkuId());
						if(info.getCode() == 0){
							SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
							redisTo.setSkuInfoVo(skuInfo);
						}
						// 4.设置当前商品的秒杀信息
						// 设置时间
						redisTo.setStartTime(session.getStartTime().getTime());
						redisTo.setEndTime(session.getEndTime().getTime());
						// 设置随机码
						redisTo.setRandomCode(randomCode);
						// sku信息放到第二个redis里 // key：活动id-skuID
						ops.put(seckillSkuVo.getPromotionSessionId() + "-" + seckillSkuVo.getSkuId(),
								JSON.toJSONString(redisTo));
						// 5.使用库存作为分布式信号量  限流，得到信号量才去改db
						RSemaphore semaphore =
								redissonClient.getSemaphore(SKUSTOCK_SEMAPHONE + randomCode);//"seckill:stock:";
						// 在信号量中设置秒杀数量
						semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
					}
				});
			});
		}
	}
}
