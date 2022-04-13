package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.MqMessageDao;
import com.atguigu.gulimall.order.entity.MqMessageEntity;
import com.atguigu.gulimall.order.service.MqMessageService;


@RabbitListener(queues = {"${myRabbitmq.queue}"})
@Service("mqMessageService")
public class MqMessageServiceImpl extends ServiceImpl<MqMessageDao, MqMessageEntity> implements MqMessageService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MqMessageEntity> page = this.page(
                new Query<MqMessageEntity>().getPage(params),
                new QueryWrapper<MqMessageEntity>()
        );

        return new PageUtils(page);
    }


    @RabbitHandler
    public void recieveMessage(Message message, OrderReturnReasonEntity content, Channel channel){
        System.out.println("接收到消息...："+message+",内容为:"+content+",传输信道为:"+channel);
    }
//    @RabbitHandler
//    public void recieveMessage2(Message message, OrderEntity content, Channel channel){
//        System.out.println("2接收到消息...："+message+",内容为:"+content+",传输信道为:"+channel);
//    }
    //注意：一个不要在一个@RabbitListener指定两个监听参数相同的@RabbitHandler，否则会导致报错
//    @RabbitHandler
//    public void recieveMessage3(Message message, OrderEntity content, Channel channel){
//        System.out.println("3接收到消息...："+message+",内容为:"+content+",传输信道为:"+channel);
//    }

    /**
     * 	1.Message message: 原生消息类型 详细信息
     * 	2.T<发送消息的类型> OrderEntity orderEntity  [Spring自动帮我们转换]
     * 	3.Channel channel: 当前传输数据的通道
     *
     * 	// 同一个消息只能被一个人收到
     *
     *
     * 	@RabbitListener： 只能标注在类、方法上 配合 @RabbitHandler
     * 	@RabbitHandler: 只能标注在方法上	[重载区分不同的消息]
     */

    @RabbitHandler
    public void receiveMessageA(Message message, OrderEntity orderEntity, Channel channel) throws IOException {
        System.out.println("OrderEntity接受到消息: " + message + "\n内容：" + orderEntity);
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) { }
        // 这个是一个数字 通道内自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 只签收当前货物 不批量签收
            channel.basicAck(deliveryTag, false);

            //deliveryTag: 货物的标签  	multiple: 是否批量拒收 requeue: 是否重新入队
			channel.basicNack(deliveryTag, false,true);
			//单个拒绝 ，无法批量拒绝，可以决定是否重新入队
			//channel.basicReject(deliveryTag,true);
        } catch (IOException e) {
            System.out.println("网络中断");
        }
        System.out.println(orderEntity.getReceiverName() + " 消息处理完成");
    }

    @RabbitHandler
    public void receiveMessageB(Message message, OrderItemEntity orderEntity, Channel channel){
        System.out.println("OrderItemEntity接受到消息: " + message + "\n内容：" + orderEntity);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            System.out.println("网络中断");
        }
        System.out.println(orderEntity.getOrderSn() + " 消息处理完成");
    }

}