package com.refinedmods.refinedstorage.api.storage.root;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage.api.storage.Actor;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface RootStorageListener extends ResourceListListener {
    /**
     * Called before a resource is actually inserted into the storage.
     * This is useful for modifying the amount before it is inserted.
     *
     * @param resource the resource
     * @param amount   the amount about to be inserted
     * @param actor    the actor
     * @return the result
     */
    InterceptResult beforeInsert(ResourceKey resource, long amount, Actor actor);

    /**
     * The result of a {@link #beforeInsert(ResourceKey, long, Actor)} interception.
     *
     * @param reserved    the amount that may not be reused for other listeners
     * @param intercepted the amount that may not be returned to the {@link RootStorage}
     */
    record InterceptResult(long reserved, long intercepted) {
        public static final InterceptResult EMPTY = new InterceptResult(0, 0);

        public InterceptResult {
            CoreValidations.validateNotNegative(reserved, "Reserved may not be negative");
            CoreValidations.validateNotNegative(intercepted, "Intercepted may not be negative");
            if (intercepted > reserved) {
                throw new IllegalArgumentException(
                    "May not intercept %d when only %d is reserved".formatted(intercepted, reserved));
            }
        }
    }
}
