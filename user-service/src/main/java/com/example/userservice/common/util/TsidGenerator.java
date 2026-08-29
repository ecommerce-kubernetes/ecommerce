package com.example.userservice.common.util;

import io.hypersistence.tsid.TSID;

public class TsidGenerator implements IdGenerator{
    @Override
    public Long generate() {
        return TSID.fast().toLong();
    }
}
