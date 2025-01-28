package com.refinedmods.refinedstorage.network.test;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.detector.DetectorNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayInputNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayOutputNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferNetworkNode;
import com.refinedmods.refinedstorage.network.test.nodefactory.ControllerNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.DetectorNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.ExporterNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.ExternalStorageNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.GridNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.ImporterNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.InterfaceNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.PatternProviderNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.RelayInputNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.RelayOutputNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.SimpleNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.StorageNetworkNodeFactory;
import com.refinedmods.refinedstorage.network.test.nodefactory.StorageTransferNetworkNodeFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(NetworkTestExtension.class)
@RegisterNetworkNode(value = ControllerNetworkNodeFactory.class, clazz = ControllerNetworkNode.class)
@RegisterNetworkNode(value = StorageNetworkNodeFactory.class, clazz = StorageNetworkNode.class)
@RegisterNetworkNode(value = ExporterNetworkNodeFactory.class, clazz = ExporterNetworkNode.class)
@RegisterNetworkNode(value = GridNetworkNodeFactory.class, clazz = GridNetworkNode.class)
@RegisterNetworkNode(value = ImporterNetworkNodeFactory.class, clazz = ImporterNetworkNode.class)
@RegisterNetworkNode(value = SimpleNetworkNodeFactory.class, clazz = SimpleNetworkNode.class)
@RegisterNetworkNode(value = InterfaceNetworkNodeFactory.class, clazz = InterfaceNetworkNode.class)
@RegisterNetworkNode(value = ExternalStorageNetworkNodeFactory.class, clazz = ExternalStorageNetworkNode.class)
@RegisterNetworkNode(value = DetectorNetworkNodeFactory.class, clazz = DetectorNetworkNode.class)
@RegisterNetworkNode(value = RelayInputNetworkNodeFactory.class, clazz = RelayInputNetworkNode.class)
@RegisterNetworkNode(value = RelayOutputNetworkNodeFactory.class, clazz = RelayOutputNetworkNode.class)
@RegisterNetworkNode(value = StorageTransferNetworkNodeFactory.class, clazz = StorageTransferNetworkNode.class)
@RegisterNetworkNode(value = PatternProviderNetworkNodeFactory.class, clazz = PatternProviderNetworkNode.class)
public @interface NetworkTest {
}
