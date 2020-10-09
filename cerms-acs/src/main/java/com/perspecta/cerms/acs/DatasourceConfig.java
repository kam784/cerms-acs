package com.perspecta.cerms.acs;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {
    @Bean
    public DataSource datasource() {
        return DataSourceBuilder.create()
                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .url("jdbc:sqlserver://USTLCSCSD0024:1455;databaseName=IAStatisticsDB")
                .username("SVC_PA2563_APPS_P")
                .password("Ptl19_vr")
                .build();
    }
}
