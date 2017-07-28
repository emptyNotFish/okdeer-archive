package com.okdeer.archive.config;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.github.pagehelper.PageHelper;
import com.okdeer.base.dal.CatMybatisPlugins;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = { "com.okdeer.archive.*.mapper", "com.okdeer.archive.*.*.mapper" })
public class MyBatisConfig implements TransactionManagementConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(MyBatisConfig.class);

	@Autowired
	private DataSourceProperties properties;

	@Bean
	public DruidDataSource getDataSource() {
		logger.debug("开始连接数据源...");
		DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName(this.properties.getDriverClass());
		ds.setUrl(this.properties.getUrl());
		ds.setUsername(this.properties.getUsername());
		ds.setPassword(this.properties.getPassword());
		ds.setMaxActive(this.properties.getMaxActive());
		ds.setInitialSize(this.properties.getInitialSize());
		ds.setValidationQuery(this.properties.getValidationQuery());
		ds.setPoolPreparedStatements(this.properties.isPoolPreparedStatements());
		ds.setMaxWait(this.properties.getMaxWait());
		ds.setMaxPoolPreparedStatementPerConnectionSize(this.properties.getMaxPoolPreparedStatementPerConnectionSize());
		ds.setTestWhileIdle(true);
		ds.setTestOnBorrow(false);
		ds.setTestOnReturn(false);

		WallFilter wallFilter = new WallFilter();
		WallConfig config = new WallConfig();
		config.setMultiStatementAllow(true);
		wallFilter.setConfig(config);
		ds.setProxyFilters(Arrays.asList(new Filter[] { wallFilter }));

		try {
			ds.setFilters(this.properties.getFilters());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ds.setConnectionProperties(this.properties.getConnectionProperties());
		return ds;
	}

	@Bean
	public Interceptor mybatisPageHelper(MybatisProperties mybatisProperties) {
		mybatisProperties.setCheckConfigLocation(true);
		mybatisProperties.setConfigLocation("classpath:mybatis/mybatis-mall-config.xml");
		// 分页插件
		PageHelper pageHelper = new PageHelper();
		Properties properties = new Properties();
		properties.setProperty("reasonable", "true");
		properties.setProperty("supportMethodsArguments", "true");
		properties.setProperty("returnPageInfo", "check");
		properties.setProperty("params", "count=countSql");
		pageHelper.setProperties(properties);

		return pageHelper;
	}

	@Bean
	public Interceptor mybatisCat(MybatisProperties mybatisProperties) {
		return new CatMybatisPlugins();
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@Bean
	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new DataSourceTransactionManager(getDataSource());
	}
}
