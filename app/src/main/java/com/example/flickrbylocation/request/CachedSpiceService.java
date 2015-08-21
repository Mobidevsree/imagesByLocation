package com.example.flickrbylocation.request;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.memory.LruCacheStringObjectPersister;

public class CachedSpiceService extends SpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager manager = new CacheManager();

        LruCacheStringObjectPersister memoryPersister = new LruCacheStringObjectPersister(500000);
        manager.addPersister(memoryPersister);

        return manager;
    }
}
