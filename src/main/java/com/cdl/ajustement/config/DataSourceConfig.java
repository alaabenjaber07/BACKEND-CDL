package com.cdl.ajustement.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource cdlNewDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:oracle:thin:@//dbdept-scan:1521/DEPTABT")
                .username("CDL_NEW")
                .password("I5NnmEpRcyrv2IAt88hc")
                .driverClassName("oracle.jdbc.OracleDriver")
                .build();
    }

    @Bean
    public DataSource cdlDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:oracle:thin:@//dbdept-scan:1521/DEPTABT")
                .username("CDL")
                .password("Rs9Ror45XsRs9bl0SP2x")
                .driverClassName("oracle.jdbc.OracleDriver")
                .build();
    }

    @Bean
    @Primary
    public DataSource routingDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("CDL_NEW", cdlNewDataSource());
        targetDataSources.put("CDL", cdlDataSource());
        
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(cdlNewDataSource());
        
        return routingDataSource;
    }
}
