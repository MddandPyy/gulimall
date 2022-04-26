package com.atguigu.gulimall.seckill.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * <p>Title: HelloSchedule</p>
 * Description：@Scheduled它不是整合cron ,如果要整合cron 需要自己引入
 * date：2020/7/6 17:03
 */
@Slf4j
// 开启异步任务
//@EnableAsync
//@EnableScheduling
//@Component
public class HelloSchedule {

	/**
	 * 在Spring中 只允许6位 [* * * ? * 1] : 每周一每秒执行一次
	 * 						[* /5 * * ? * 1] : 每周一 每5秒执行一次
	 * 	方法1.定时任务不应阻塞 [默认是阻塞的]
	 * 	方法2.定时任务线程池 spring.task.scheduling.pool.size=5  自动配置类
	 * 	方法3.让定时任务异步执行   自动配置类 TaskExecutionAutoConfiguration
	 */
//	@Async
//	@Scheduled(cron = "*/3 * * ? * *")
	public void hello(){
		log.info("定时任务...");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) { }
	}
}
