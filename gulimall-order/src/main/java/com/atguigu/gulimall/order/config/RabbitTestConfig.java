package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTestConfig {
    //创建queue
    @Bean(name = "queue1") // 指定该队列bean在spring容器中的名字 ，可以标记是使用的哪个bean，见下面Qualifier（"queue1"）
    public Queue queueMessage(){
        return new Queue("test-topic-queue1");  //  test-topic-queue1为此消息队列的名字，也是rabbitmq服务器中创建的队列名
    }
    //创建queue
    @Bean(name = "queue2")
    public Queue queueMessages(){
        return new Queue("test-topic-queue2");
    }

    //创建交换机，指定交换机类型和名字
    @Bean(name = "exchange1")
    public TopicExchange exchange(){
        return new TopicExchange("test-topic-exchange");
    }

    //创建交换机，指定交换机类型和名字
//    @Bean(name = "exchange2")
//    public TopicExchange exchange2(){
//        return new TopicExchange("test-topic-exchange2");
//    }


    //创建binding关联关系，将queue与交换机绑定，使用@Qualifier("queue1")指明需要用上面哪个queue的bean来关联。
    //BindingBuilder.bind(queueMessage).to(exchange).with("message.push");
    //创建关联关系，绑定队列queue到交换机exchange，并指定routingKey。
    //此处因为spring容器中只有一个TopicExchange，所以不需要指定名字，最好还是指定需要绑定的交换机的名字
    @Bean
    public Binding bindingExchangeMessage(@Qualifier("queue1") Queue queue, @Qualifier("exchange1") TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("message.push");
    }
    //
    @Bean
    public  Binding bindingExchangeUserNodejsTopic(@Qualifier("queue2")Queue queue,@Qualifier("exchange1")TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("message.*");
    }
}
