package com.refinedmods.refinedstorage.network.test.fixtures;

import com.refinedmods.refinedstorage.api.storage.Actor;

public final class ActorFixture implements Actor {
    public static final ActorFixture INSTANCE = new ActorFixture();

    private ActorFixture() {
    }

    @Override
    public String getName() {
        return "Fake";
    }
}
