package io.woolford;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.hbase.HbaseTemplate;

@Configuration
public class DataConfig {


    @Bean
    HBaseConfiguration hBaseConfiguration(){
        HBaseConfiguration hBaseConfiguration = new HBaseConfiguration();
        hBaseConfiguration.addResource("hbase-site.xml");
        return hBaseConfiguration;
    }

    @Bean
    HbaseTemplate hbaseTemplate() {
        return new HbaseTemplate(hBaseConfiguration());
    }


}
