package com.refinedmods.refinedstorage.api.storage;

import org.apiguardian.api.API;

/**
 * Represents an actor that can perform storage actions.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
@FunctionalInterface
public interface Actor {
    /**
     * An unidentified {@link Actor}.
     */
    Actor EMPTY = () -> "Empty";

    /**
     * @return the name of the actor
     */
    String getName();
}
