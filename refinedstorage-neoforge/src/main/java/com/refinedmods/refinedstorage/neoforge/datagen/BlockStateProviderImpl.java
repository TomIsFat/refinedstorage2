package com.refinedmods.refinedstorage.neoforge.datagen;

import com.refinedmods.refinedstorage.common.constructordestructor.AbstractConstructorDestructorBlock;
import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.controller.AbstractControllerBlock;
import com.refinedmods.refinedstorage.common.controller.ControllerEnergyType;
import com.refinedmods.refinedstorage.common.detector.DetectorBlock;
import com.refinedmods.refinedstorage.common.networking.NetworkReceiverBlock;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterBlock;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.DefaultDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.HorizontalDirectionType;

import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class BlockStateProviderImpl extends BlockStateProvider {
    private static final String BLOCK_PREFIX = "block";

    private final ExistingFileHelper existingFileHelper;

    public BlockStateProviderImpl(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void registerStatesAndModels() {
        registerCables();
        registerCableLike(Blocks.INSTANCE.getImporter(), "importer");
        registerCableLike(Blocks.INSTANCE.getExporter(), "exporter");
        registerCableLike(Blocks.INSTANCE.getExternalStorage(), "external_storage");
        registerControllers();
        registerGrids();
        registerDetectors();
        registerWirelessTransmitters();
        registerConstructorDestructor(Blocks.INSTANCE.getConstructor(), "constructor");
        registerConstructorDestructor(Blocks.INSTANCE.getDestructor(), "destructor");
        registerNetworkReceivers();
        registerNetworkTransmitters();
        registerSecurityManagers();
        registerRelays();
        registerDiskInterfaces();
        registerAutocrafters();
        registerAutocrafterManagers();
        registerAutocraftingMonitors();
    }

    private void registerCables() {
        Blocks.INSTANCE.getCable().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.addModels(
                builder.partialState(),
                ConfiguredModel.builder().modelFile(getCableModel(color)).build()
            );
        });
    }

    private void registerCableLike(final BlockColorMap<?, ?> blockMap, final String type) {
        final ModelFile model = modelFile(createIdentifier("block/" + type));
        blockMap.forEach((color, id, block) -> {
            final MultiPartBlockStateBuilder builder = getMultipartBuilder(block.get());
            final var cablePart = builder.part();
            cablePart.modelFile(getCableModel(color)).addModel();
            for (final Direction direction : Direction.values()) {
                final var part = builder.part();
                addDirectionalRotation(direction, part);
                part.modelFile(model).addModel()
                    .condition(DefaultDirectionType.FACE_CLICKED.getProperty(), direction);
            }
        });
    }

    private static void addDirectionalRotation(
        final Direction direction,
        final ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> part
    ) {
        switch (direction) {
            case UP -> part.rotationX(270);
            case SOUTH -> part.rotationX(180);
            case DOWN -> part.rotationX(90);
            case WEST -> part.rotationY(270);
            case EAST -> part.rotationY(90);
            default -> {
                // do nothing
            }
        }
    }

    private void registerGrids() {
        Blocks.INSTANCE.getGrid().forEach((color, id, block) -> configureActiveColoredDirectionalBlock(
            color,
            block,
            "grid"
        ));
        Blocks.INSTANCE.getCraftingGrid().forEach((color, id, block) -> configureActiveColoredDirectionalBlock(
            color,
            block,
            "crafting_grid"
        ));
        Blocks.INSTANCE.getPatternGrid().forEach((color, id, block) -> configureActiveColoredDirectionalBlock(
            color,
            block,
            "pattern_grid"
        ));
    }

    private void registerControllers() {
        Blocks.INSTANCE.getController().forEach((color, id, block) -> configureControllerVariants(color, block));
        Blocks.INSTANCE.getCreativeController().forEach(
            (color, id, block) -> configureControllerVariants(color, block)
        );
    }

    private void configureControllerVariants(final DyeColor color, final Supplier<? extends Block> block) {
        final ModelFile off = modelFile(createIdentifier("block/controller/off"));
        final ModelFile nearlyOff = modelFile(createIdentifier("block/controller/nearly_off"));
        final ModelFile nearlyOn = modelFile(createIdentifier("block/controller/nearly_on"));
        final var builder = getVariantBuilder(block.get());
        builder.addModels(
            builder.partialState().with(AbstractControllerBlock.ENERGY_TYPE, ControllerEnergyType.OFF),
            ConfiguredModel.builder().modelFile(off).buildLast()
        );
        builder.addModels(
            builder.partialState().with(AbstractControllerBlock.ENERGY_TYPE, ControllerEnergyType.NEARLY_OFF),
            ConfiguredModel.builder().modelFile(nearlyOff).buildLast()
        );
        builder.addModels(
            builder.partialState().with(AbstractControllerBlock.ENERGY_TYPE, ControllerEnergyType.NEARLY_ON),
            ConfiguredModel.builder().modelFile(nearlyOn).buildLast()
        );
        builder.addModels(
            builder.partialState().with(AbstractControllerBlock.ENERGY_TYPE, ControllerEnergyType.ON),
            ConfiguredModel.builder()
                .modelFile(modelFile(createIdentifier("block/controller/" + color.getName())))
                .buildLast()
        );
    }

    private void registerDetectors() {
        final ModelFile unpowered = modelFile(createIdentifier("block/detector/unpowered"));
        Blocks.INSTANCE.getDetector().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> registerDetector(unpowered, block.get(), blockState));
        });
    }

    private void registerWirelessTransmitters() {
        final ModelFile inactive = modelFile(createIdentifier("block/wireless_transmitter/inactive"));
        Blocks.INSTANCE.getWirelessTransmitter().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                if (Boolean.TRUE.equals(blockState.getValue(AbstractActiveColoredDirectionalBlock.ACTIVE))) {
                    model.modelFile(modelFile(createIdentifier("block/wireless_transmitter/" + color.getName())));
                } else {
                    model.modelFile(inactive);
                }
                final Direction direction = blockState.getValue(DefaultDirectionType.FACE_CLICKED.getProperty());
                addRotation(model, direction);
                return model.build();
            });
        });
    }

    private void registerConstructorDestructor(final BlockColorMap<?, ?> blockMap, final String type) {
        final ModelFile activeModel = modelFile(createIdentifier(BLOCK_PREFIX + "/" + type + "/active"));
        final ModelFile inactiveModel = modelFile(createIdentifier(BLOCK_PREFIX + "/" + type + "/inactive"));
        blockMap.forEach((color, id, block) -> {
            final MultiPartBlockStateBuilder builder = getMultipartBuilder(block.get());
            final var cablePart = builder.part();
            cablePart.modelFile(getCableModel(color)).addModel();
            for (final Direction direction : Direction.values()) {
                final var part = builder.part();
                addDirectionalRotation(direction, part);
                part.modelFile(activeModel)
                    .addModel()
                    .condition(DefaultDirectionType.FACE_CLICKED.getProperty(), direction)
                    .condition(AbstractConstructorDestructorBlock.ACTIVE, true)
                    .end();
                part.modelFile(inactiveModel)
                    .addModel()
                    .condition(DefaultDirectionType.FACE_CLICKED.getProperty(), direction)
                    .condition(AbstractConstructorDestructorBlock.ACTIVE, false)
                    .end();
            }
        });
    }

    private ModelFile getCableModel(final DyeColor color) {
        return modelFile(createIdentifier("block/cable/" + color.getName()));
    }

    private ConfiguredModel[] registerDetector(final ModelFile unpowered,
                                               final DetectorBlock block,
                                               final BlockState blockState) {
        final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
        if (Boolean.TRUE.equals(blockState.getValue(DetectorBlock.POWERED))) {
            model.modelFile(modelFile(createIdentifier("block/detector/" + block.getColor().getName())));
        } else {
            model.modelFile(unpowered);
        }
        final Direction direction = blockState.getValue(DefaultDirectionType.FACE_CLICKED.getProperty());
        addRotation(model, direction);
        return model.build();
    }

    private void registerNetworkReceivers() {
        final ModelFile inactive = modelFile(createIdentifier("block/network_receiver/inactive"));
        Blocks.INSTANCE.getNetworkReceiver().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                if (Boolean.TRUE.equals(blockState.getValue(NetworkReceiverBlock.ACTIVE))) {
                    model.modelFile(modelFile(createIdentifier("block/network_receiver/" + color.getName())));
                } else {
                    model.modelFile(inactive);
                }
                return model.build();
            });
        });
    }

    private void registerNetworkTransmitters() {
        final ModelFile inactive = modelFile(createIdentifier("block/network_transmitter/inactive"));
        final ModelFile error = modelFile(createIdentifier("block/network_transmitter/error"));
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                switch (blockState.getValue(NetworkTransmitterBlock.STATE)) {
                    case ACTIVE ->
                        model.modelFile(modelFile(createIdentifier("block/network_transmitter/" + color.getName())));
                    case ERROR -> model.modelFile(error);
                    case INACTIVE -> model.modelFile(inactive);
                }
                return model.build();
            });
        });
    }

    private void registerSecurityManagers() {
        final ModelFile inactive = modelFile(createIdentifier("block/security_manager/inactive"));
        Blocks.INSTANCE.getSecurityManager().forEach((color, id, block) -> {
            final ModelFile active = modelFile(createIdentifier("block/security_manager/" + color.getName()));
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                if (Boolean.TRUE.equals(blockState.getValue(AbstractActiveColoredDirectionalBlock.ACTIVE))) {
                    model.modelFile(active);
                } else {
                    model.modelFile(inactive);
                }
                final Direction direction = HorizontalDirectionType.INSTANCE.extractDirection(blockState.getValue(
                    HorizontalDirectionType.INSTANCE.getProperty()
                ));
                addRotationFrontFacingNorth(model, BiDirection.forHorizontal(direction));
                return model.build();
            });
        });
    }

    private void registerRelays() {
        final ModelFile inactive = modelFile(createIdentifier("block/relay/inactive"));
        Blocks.INSTANCE.getRelay().forEach((color, id, block) -> {
            final ModelFile active = modelFile(createIdentifier("block/relay/" + color.getName()));
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                if (Boolean.TRUE.equals(blockState.getValue(AbstractActiveColoredDirectionalBlock.ACTIVE))) {
                    model.modelFile(active);
                } else {
                    model.modelFile(inactive);
                }
                final Direction direction = blockState.getValue(DefaultDirectionType.FACE_CLICKED.getProperty());
                final BiDirection biDirection = BiDirection.forDirection(direction);
                addRotationFrontFacingNorth(model, biDirection);
                return model.build();
            });
        });
    }

    private void registerDiskInterfaces() {
        Blocks.INSTANCE.getDiskInterface().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.addModels(
                builder.partialState(),
                ConfiguredModel.builder().modelFile(
                    modelFile(createIdentifier("block/disk_interface/" + color.getName()))
                ).build()
            );
        });
    }

    private void registerAutocrafters() {
        final ModelFile inactive = modelFile(createIdentifier(BLOCK_PREFIX + "/autocrafter/inactive"));
        Blocks.INSTANCE.getAutocrafter().forEach((color, id, block) -> {
            final ModelFile active = modelFile(createIdentifier(BLOCK_PREFIX + "/autocrafter/" + color.getName()));
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                if (Boolean.TRUE.equals(blockState.getValue(AbstractActiveColoredDirectionalBlock.ACTIVE))) {
                    model.modelFile(active);
                } else {
                    model.modelFile(inactive);
                }
                final Direction direction = blockState.getValue(DefaultDirectionType.FACE_CLICKED.getProperty());
                addAutocrafterRotation(model, direction);
                return model.build();
            });
        });
    }

    private void registerAutocrafterManagers() {
        Blocks.INSTANCE.getAutocrafterManager().forEach((color, id, block) -> configureActiveColoredDirectionalBlock(
            color,
            block,
            "autocrafter_manager"
        ));
    }

    private void registerAutocraftingMonitors() {
        Blocks.INSTANCE.getAutocraftingMonitor().forEach((color, id, block) -> configureActiveColoredDirectionalBlock(
            color,
            block,
            "autocrafting_monitor"
        ));
    }

    private void configureActiveColoredDirectionalBlock(final DyeColor color,
                                                        final Supplier<? extends Block> block,
                                                        final String name) {
        final ModelFile inactive = modelFile(createIdentifier(BLOCK_PREFIX + "/" + name + "/inactive"));
        final ModelFile active = modelFile(createIdentifier(BLOCK_PREFIX + "/" + name + "/" + color.getName()));
        final var builder = getVariantBuilder(block.get());
        builder.forAllStates(blockState -> {
            final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
            if (Boolean.TRUE.equals(blockState.getValue(AbstractActiveColoredDirectionalBlock.ACTIVE))) {
                model.modelFile(active);
            } else {
                model.modelFile(inactive);
            }
            final BiDirection direction = blockState.getValue(BiDirectionType.INSTANCE.getProperty());
            addRotationFrontFacingNorth(model, direction);
            return model.build();
        });
    }

    private void addRotation(final ConfiguredModel.Builder<?> model, final Direction direction) {
        final int rotationX;
        if (direction.getAxis() == Direction.Axis.Y) {
            rotationX = direction == Direction.UP ? 180 : 0;
        } else {
            rotationX = direction.getAxis().isHorizontal() ? 90 : 0;
        }
        final int rotationY = direction.getAxis().isVertical() ? 0 : ((int) direction.toYRot()) % 360;
        model.rotationX(rotationX);
        model.rotationY(rotationY);
    }

    private void addRotationFrontFacingNorth(final ConfiguredModel.Builder<?> model, final BiDirection direction) {
        final int x = (int) direction.getVec().x();
        final int y = (int) direction.getVec().y();
        final int z = (int) direction.getVec().z();
        switch (direction) {
            case UP_EAST, UP_NORTH, UP_SOUTH, UP_WEST, DOWN_EAST, DOWN_WEST, DOWN_SOUTH, DOWN_NORTH ->
                model.rotationX(x * -1).rotationY(z);
            case EAST, WEST -> model.rotationY(y + 180);
            case NORTH, SOUTH -> model.rotationY(y);
        }
    }

    private void addAutocrafterRotation(final ConfiguredModel.Builder<?> model, final Direction direction) {
        if (direction.getAxis().isHorizontal()) {
            model.rotationX(90);
        } else if (direction == Direction.DOWN) {
            model.rotationX(180);
        }
        model.rotationY(direction.getAxis().isVertical() ? 0 : (((int) direction.toYRot()) + 180) % 360);
    }

    private ModelFile modelFile(final ResourceLocation location) {
        return new ModelFile.ExistingModelFile(location, existingFileHelper);
    }
}
