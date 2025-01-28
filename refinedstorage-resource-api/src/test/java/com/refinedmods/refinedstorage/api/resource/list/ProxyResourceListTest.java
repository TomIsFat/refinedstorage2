package com.refinedmods.refinedstorage.api.resource.list;

class ProxyResourceListTest extends AbstractMutableResourceListTest {
    @Override
    protected MutableResourceList createList() {
        return new AbstractProxyMutableResourceList(MutableResourceListImpl.create()) {
        };
    }
}
