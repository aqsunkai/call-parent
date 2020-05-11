package com.erp.call.web.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class IDGeneratorUtil {

    private Snowflake snowflake;

    @PostConstruct
    private void init() {
        int workerId = RandomUtil.randomInt(1, 31);
        snowflake = IdUtil.createSnowflake(workerId, 1);
    }

    public String snowflakeId() {
        long id = snowflake.nextId();
        return id > 0L ? String.valueOf(id) : String.valueOf(-id);
    }

}
