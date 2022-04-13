package com.atguigu.gulimall.order.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

/**
 *
 **/

@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;
//
//    @Primary
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        this.rabbitTemplate = rabbitTemplate;
//        rabbitTemplate.setMessageConverter(messageConverter());
//        initRabbitTemplate();
//        return rabbitTemplate;
//    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1、服务收到消息就会回调
     *      1、spring.rabbitmq.publisher-confirms: true
     *      2、设置确认回调
     * 2、消息正确抵达队列就会进行回调
     *      1、spring.rabbitmq.publisher-returns: true
     *         spring.rabbitmq.template.mandatory: true
     *      2、设置确认回调ReturnCallback
     *
     * 3、消费端确认(保证每个消息都被正确消费，此时才可以broker删除这个消息)
     *
     */
    @PostConstruct  //MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {

        /**
         * 1、只要消息抵达Broker就ack=true
         * correlationData：当前消息的唯一关联数据(这个是消息的唯一id)
         * ack：消息是否成功收到
         * cause：失败的原因
         * confirm...correlationData[CorrelationData [id=56edcdc0-8a44-4a7d-a07e-59e18a315da4]]==>ack:[true]==>cause:[null]
         */
        //设置确认回调
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
            System.out.println("confirm...correlationData["+correlationData+"]==>ack:["+ack+"]==>cause:["+cause+"]");
        });


        /**
         * 只要消息没有投递给指定的队列，就触发这个失败回调
         * message：投递失败的消息详细信息
         * replyCode：回复的状态码
         * replyText：回复的文本内容
         * exchange：当时这个消息发给哪个交换机
         * routingKey：当时这个消息用哪个路邮键
         * 可以将失败的消息，存储到数据库，定时重新发送
         * Fail Message[
         * (Body:'{"id":null,"memberId":null,"orderSn":null,"couponId":null,"createTime":null,
         * "memberUsername":"测试不同类型-1","totalAmount":null,"payAmount":null,"freightAmount":null,
         * "promotionAmount":null,"integrationAmount":null,"couponAmount":null,"discountAmount":null,
         * "payType":null,"sourceType":null,"status":null,"deliveryCompany":null,"deliverySn":null,
         * "autoConfirmDay":null,"integration":null,"growth":null,"billType":null,"billHeader":null,
         * "billContent":null,"billReceiverPhone":null,"billReceiverEmail":null,"receiverName":null,
         * "receiverPhone":null,"receiverPostCode":null,"receiverProvince":null,"receiverCity":null,
         * "receiverRegion":null,"receiverDetailAddress":null,"note":null,"confirmStatus":null,
         * "deleteStatus":null,"useIntegration":null,"paymentTime":null,"deliveryTime":null,"receiveTime":null,
         * "commentTime":null,"modifyTime":null}'
         * MessageProperties [headers={spring_returned_message_correlation=56edcdc0-8a44-4a7d-a07e-59e18a315da4,
         * __TypeId__=com.atguigu.gulimall.order.entity.OrderEntity},
         * contentType=application/json, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT,
         * priority=0, deliveryTag=0])]
         * ==>replyCode[312]==>replyText[NO_ROUTE]==>exchange[hello-java-exchange]==>routingKey[hello2.java]
         */
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey) -> {
            System.out.println("Fail Message["+message+"]==>replyCode["+replyCode+"]" +
                    "==>replyText["+replyText+"]==>exchange["+exchange+"]==>routingKey["+routingKey+"]");
        });
    }
}
