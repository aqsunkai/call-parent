package com.erp.call.web.util.id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReferenceArray;


public class CircleArray {
    private Logger logger = LoggerFactory.getLogger(CircleArray.class);
    private final int capacity;
    private AtomicReferenceArray<IdSeed> idSeeds;

    public CircleArray(int capacity) {
        if(capacity<2){
            throw new IllegalArgumentException("capacity must bigger than 2");
        }
        this.capacity = capacity;
        this.idSeeds = new AtomicReferenceArray<IdSeed>(capacity);
    }

    public long generateSequence(long timeStamp){
        int ix = (int)(timeStamp % this.capacity);
        IdSeed seed = this.idSeeds.get(ix);
        if(seed==(null)||seed.getTimeStamp() != timeStamp){
            IdSeed newSeed = new IdSeed(timeStamp);
            this.idSeeds.compareAndSet(ix,seed,newSeed);
            try {
                Thread.sleep(100L);
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            return this.idSeeds.get(ix).increment();
        }else {
            return seed.increment();
        }
    }
}
