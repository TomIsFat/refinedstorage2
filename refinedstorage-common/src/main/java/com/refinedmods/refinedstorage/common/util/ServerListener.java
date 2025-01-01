package com.refinedmods.refinedstorage.common.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public final class ServerListener {
    @Nullable
    private static ExecutorService autocraftingPool;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerListener.class);
    private static final Deque<Runnable> ACTIONS = new ArrayDeque<>();

    private ServerListener() {
    }

    public static void tick() {
        synchronized (ACTIONS) {
            Runnable action;
            while ((action = ACTIONS.poll()) != null) {
                action.run();
            }
        }
    }

    public static void queue(final Runnable runnable) {
        synchronized (ACTIONS) {
            ACTIONS.add(runnable);
        }
    }

    private static ExecutorService createAutocraftingPool() {
        return Executors.newFixedThreadPool(4, new BasicThreadFactory.Builder()
            .namingPattern("refinedstorage-autocrafting-%d")
            .build());
    }

    public static ExecutorService getAutocraftingPool() {
        return requireNonNull(autocraftingPool, "Autocrafting pool is not initialized");
    }

    public static void starting() {
        if (autocraftingPool != null) {
            LOGGER.debug("Previous autocrafting pool is still active, stopping");
            stopPool(autocraftingPool);
            autocraftingPool = null;
        }
        LOGGER.debug("Creating new autocrafting pool");
        autocraftingPool = createAutocraftingPool();
    }

    public static void stopped() {
        if (autocraftingPool != null) {
            LOGGER.debug("Stopping autocrafting pool");
            stopPool(autocraftingPool);
            autocraftingPool = null;
        } else {
            LOGGER.debug("There was no autocrafting pool to stop?");
        }
    }

    private static void stopPool(final ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                LOGGER.warn("Autocrafting pool did not terminate in time, forcing shutdown");
                pool.shutdownNow();
            }
        } catch (final InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for autocrafting pool to terminate", e);
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
