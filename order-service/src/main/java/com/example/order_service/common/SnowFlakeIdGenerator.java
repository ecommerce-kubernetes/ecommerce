package com.example.order_service.common;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SnowFlakeIdGenerator {

    private final static long epoch = 1288834974657L;
    private final static long nodeIdBits = 10L;
    private final static long sequenceBits = 12L;

    private final static long maxNodeId = ~(-1L << nodeIdBits);
    private final static long sequenceMask = ~(-1L << sequenceBits);

    private final long nodeShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + nodeIdBits;

    private final long nodeId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private final Environment env;

    public SnowFlakeIdGenerator(Environment env){
        this.env = env;
        long port = env.getProperty("local.server.port", Integer.class, 0);

        this.nodeId = port % 1024;
        if(nodeId > maxNodeId || nodeId < 0){
            throw new IllegalArgumentException("Invalid nodeId");
        }
    }

    public synchronized long nextId(){
        long timestamp = System.currentTimeMillis();
        if(timestamp < lastTimestamp){
            throw new RuntimeException();
        }

        if(timestamp == lastTimestamp){
            sequence = (sequence + 1) & sequenceMask;
            if(sequence == 0){
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - epoch) << timestampLeftShift)
                | (nodeId << nodeShift)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp){
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp){
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
