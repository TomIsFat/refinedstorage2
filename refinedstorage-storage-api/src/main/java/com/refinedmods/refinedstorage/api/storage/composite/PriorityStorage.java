package com.refinedmods.refinedstorage.api.storage.composite;

import com.refinedmods.refinedstorage.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.6")
public class PriorityStorage extends AbstractProxyStorage implements PriorityProvider {
    private int insertPriority;
    private int extractPriority;

    private PriorityStorage(final int insertPriority, final int extractPriority, final Storage delegate) {
        super(delegate);
        this.insertPriority = insertPriority;
        this.extractPriority = extractPriority;
    }

    public static PriorityStorage of(final Storage delegate, final int insertPriority, final int extractPriority) {
        return new PriorityStorage(insertPriority, extractPriority, delegate);
    }

    public void setInsertPriority(final int insertPriority) {
        this.insertPriority = insertPriority;
    }

    public void setExtractPriority(final int extractPriority) {
        this.extractPriority = extractPriority;
    }

    @Override
    public int getInsertPriority() {
        return insertPriority;
    }

    @Override
    public int getExtractPriority() {
        return extractPriority;
    }
}

