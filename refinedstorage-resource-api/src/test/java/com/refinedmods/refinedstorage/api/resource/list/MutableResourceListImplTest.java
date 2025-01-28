package com.refinedmods.refinedstorage.api.resource.list;

class MutableResourceListImplTest extends AbstractMutableResourceListTest {
    @Override
    protected MutableResourceList createList() {
        return MutableResourceListImpl.create();
    }
}
