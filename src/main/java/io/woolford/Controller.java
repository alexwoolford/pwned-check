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

import java.util.Map;

import static com.oracle.jrockit.jfr.ContentType.Bytes;


@RestController
@EnableAutoConfiguration
public class Controller {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseTemplate hbaseTemplate;

    @RequestMapping(value = "/password-lookup", method = RequestMethod.POST)
    private LookupRecord lookupPassword(@RequestBody String password) throws Exception {

        String sha1 = DigestUtils.sha1Hex(password).toUpperCase();

        RowMapper mapper = new RowMapper<Object>() {
            @Override
            public Boolean mapRow(Result result, int rowNum) throws Exception {
                return result.containsColumn("hash_exists".getBytes(), "hash_exists".getBytes());
            }
        };

        LookupRecord lookupRecord = new LookupRecord();
        lookupRecord.setSha1(sha1);
        lookupRecord.setPassword(password);
        lookupRecord.setHashExists((Boolean) hbaseTemplate.get("password", sha1, mapper));
        
        return lookupRecord;

    }

}