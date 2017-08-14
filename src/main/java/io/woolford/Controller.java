package io.woolford;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.web.bind.annotation.*;


@RestController
@EnableAutoConfiguration
public class Controller {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseTemplate hbaseTemplate;

    @RequestMapping(value = "/password-lookup", method = RequestMethod.POST)
    private String lookupPassword(@RequestBody String password) throws Exception {

        String sha1 = DigestUtils.sha1Hex(password).toUpperCase();

        RowMapper<String> mapper = new RowMapper<String>() {
            @Override
            public String mapRow(Result result, int rowNum) throws Exception {
                return result.toString();
            }
        };

        String result = hbaseTemplate.get("password", sha1, "hash_exists", "hash_exists", mapper);

        return result;

    }

}