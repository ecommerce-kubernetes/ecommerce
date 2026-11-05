package com.example.userservice.common.util;

import io.hypersistence.tsid.TSID;
import org.springframework.stereotype.Component;

@Component
public class TsidGenerator implements IdGenerator{
    @Override
    public Long generate() {
        return TSID.fast().toLong();
    }
}
