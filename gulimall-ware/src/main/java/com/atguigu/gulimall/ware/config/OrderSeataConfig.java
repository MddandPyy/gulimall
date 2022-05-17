package com.atguigu.gulimall.ware.config;

import com.zaxxer.hikari.HikariDataSource;
//import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Description：
 */
@Configuration
public class OrderSeataConfig {

//	// 代理数据源，seata事务使用
//	@Bean
//	public DataSource dataSource(DataSourceProperties dataSourceProperties){
//		HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
//		if(StringUtils.hasText(dataSourceProperties.getName())){
//			dataSource.setPoolName(dataSourceProperties.getName());
//		}
//		return new DataSourceProxy(dataSource);
//	}
}
