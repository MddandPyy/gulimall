package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.atguigu.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
//@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    OrderService orderService;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Test
    public void sendMessageTest() {
//        String msg = "Hello World";
//        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",
//                msg);

        for (int i = 0; i <10 ; i++) {
            if(i%2 == 0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("测试-"+i);
                reasonEntity.setStatus(1);
                reasonEntity.setSort(2);

                //1、发送消息,如果发送的消息是个对象，会使用序列化机制，将对象写出去，对象必须实现Serializable接口

                //2、发送的对象类型的消息，可以是一个json
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",
                        reasonEntity,new CorrelationData(UUID.randomUUID().toString()));
                log.info("消息发送完成:{}",reasonEntity);
            }else{
                OrderEntity  orderEntity = new OrderEntity();
                orderEntity.setMemberUsername("测试不同类型-"+i);
                rabbitTemplate.convertAndSend("hello-java-exchange","hello2.java",
                        orderEntity,new CorrelationData(UUID.randomUUID().toString()));
                log.info("消息发送完成:{}",orderEntity);

            }

        }


    }

    @Test
    public void sendMessageTest2() {
        rabbitTemplate.convertAndSend("test-topic-exchange","message.push", "测试topic");
    }


    @Test
    void contextLoads() {
        System.out.println(orderService.count());
    }



    /**
     * 1、如何创建Exchange、Queue、Binding
     *      1）、使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    @Test
    public void createExchange() {
        Exchange directExchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功：","hello-java-exchange");
    }

    @Test
    public void testCreateQueue() {
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功：","hello-java-queue");
    }


    @Test
    public void createBinding() {

        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功：","hello-java-binding");

    }
}
