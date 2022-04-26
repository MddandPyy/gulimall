package com.atguigu.gulimall.order.config;

import com.atguigu.common.constant.RabbitInfo;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Description：容器中的所有bean都会自动创建到RabbitMQ中 [RabbitMQ没有这个队列、交换机、绑定]
 * date：2020/7/3 17:03
 * 创建交换机、队列、bind
 */
@Configuration
public class OrderMQConfig {

	/**
	 * 创建死信队列，也是一个普通的队列，只不过指定了消息的过期时间，以及过期后转发的交换机和routing-key
	 * 1、创建队列时指定死信交换机，
	 * 2、以及重新发送到死信交换机时携带的routing-key
	 * 3、消息的淘过期（时间到了，消息变为死信）
	 */
	@Bean
	public Queue orderDelayQueue(){
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x-dead-letter-exchange", RabbitInfo.Order.exchange);
		// 死信队列的re路由key
		arguments.put("x-dead-letter-routing-key",  RabbitInfo.Order.releaseRoutingKey);
		arguments.put("x-message-ttl",  RabbitInfo.Order.ttl);
		Queue queue = new Queue( RabbitInfo.Order.delayQueue, true, false, false, arguments);
		return queue;
	}

	/**
	 * 死信交换机绑定的队列，用于处理解单业务
	 */
	@Bean
	public Queue orderReleaseOrderQueue(){
		Queue queue = new Queue( RabbitInfo.Order.releaseQueue,
				true,
				false,
				false);
		return queue;
	}

	/**
	 * 死信交换机，也就是一个普通的交换机，死信交换机没什么特殊，特殊的是死信队列对死信的处理方式
	 */
	@Bean
	public Exchange orderEventExchange(){
		return new TopicExchange( RabbitInfo.Order.exchange,
				true, false);
	}



	/**
	 * 绑定交换机与死信队列的binding
	 */
	@Bean
	public Binding orderCreateOrderBinding(){

		return new Binding( RabbitInfo.Order.delayQueue, Binding.DestinationType.QUEUE,
				RabbitInfo.Order.exchange,  RabbitInfo.Order.delayRoutingKey, null);
	}

	/**
	 * 绑定交换机与解订单队列的binding
	 */
	@Bean
	public Binding orderReleaseOrderBinding(){

		return new Binding( RabbitInfo.Order.releaseQueue, Binding.DestinationType.QUEUE,
				RabbitInfo.Order.exchange,  RabbitInfo.Order.releaseRoutingKey, null);
	}

	/**
	 * 订单释放直接和库存释放进行绑定
	 */
	@Bean
	public Binding orderReleaseOtherBinding(){

		return new Binding(RabbitInfo.Stock.releaseQueue, Binding.DestinationType.QUEUE,
				RabbitInfo.Order.exchange,
				RabbitInfo.Order.orderreleasestock, null);
	}

	/**
	 * 秒杀订单queue，流量削峰
	 */
	@Bean
	public Queue orderSecKillQueue(){
		return new Queue(RabbitInfo.SecKill.delayQueue,
				true, false, false);
	}


	/**
	 * 秒杀订单与交换机进行绑定
	 */
	@Bean
	public Binding orderSecKillQueueBinding(){
		return new Binding(RabbitInfo.SecKill.delayQueue, Binding.DestinationType.QUEUE,
				RabbitInfo.Order.exchange, RabbitInfo.SecKill.delayRoutingKey, null);
	}

	/**
	 * 测试死信队列
	 */
//	@RabbitListener(queues = RabbitInfo.Order.releaseQueue)
//	public void  test(OrderEntity entity, Channel channel, Message message) throws IOException {
//		System.out.println("收到过期订单消息，准备关闭订单"+entity.getOrderSn());
//		channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//	}
}
