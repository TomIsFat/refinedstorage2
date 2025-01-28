package com.refinedmods.refinedstorage.api.storage;

public final class ActorFixtures {
    private ActorFixtures() {
    }

    public static final class ActorFixture1 implements Actor {
        public static final Actor INSTANCE = new ActorFixture1();

        @Override
        public String getName() {
            return "Source1";
        }
    }

    public static final class ActorFixture2 implements Actor {
        public static final Actor INSTANCE = new ActorFixture2();

        @Override
        public String getName() {
            return "Source2";
        }
    }
}
