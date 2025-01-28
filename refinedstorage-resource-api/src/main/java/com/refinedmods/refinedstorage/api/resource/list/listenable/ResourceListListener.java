package com.refinedmods.refinedstorage.api.resource.list.listenable;

import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;

import org.apiguardian.api.API;

/**
 * A listener for resource list operations. Can be used on a {@link ListenableResourceList}.
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface ResourceListListener {
    /**
     * Called when a list operation has occurred.
     *
     * @param change the change
     */
    void changed(MutableResourceList.OperationResult change);
}
