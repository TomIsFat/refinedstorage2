package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.api.grid.query.GridQueryParserException;
import com.refinedmods.refinedstorage.api.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage.api.grid.view.GridView;
import com.refinedmods.refinedstorage.api.grid.view.GridViewBuilder;
import com.refinedmods.refinedstorage.api.grid.view.GridViewBuilderImpl;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.Config;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.grid.view.PlatformGridResource;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.grid.strategy.ClientGridExtractionStrategy;
import com.refinedmods.refinedstorage.common.grid.strategy.ClientGridInsertionStrategy;
import com.refinedmods.refinedstorage.common.grid.strategy.ClientGridScrollingStrategy;
import com.refinedmods.refinedstorage.common.grid.view.CompositeGridResourceFactory;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSizeListener;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.parser.ParserOperatorMappings;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGridContainerMenu extends AbstractResourceContainerMenu
    implements GridWatcher, GridInsertionStrategy, GridExtractionStrategy, GridScrollingStrategy, ScreenSizeListener,
    PreviewProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGridContainerMenu.class);
    private static final GridQueryParserImpl QUERY_PARSER = new GridQueryParserImpl(
        LexerTokenMappings.DEFAULT_MAPPINGS,
        ParserOperatorMappings.DEFAULT_MAPPINGS,
        Map.of(
            "@", Set.of(GridResourceAttributeKeys.MOD_ID, GridResourceAttributeKeys.MOD_NAME),
            "$", Set.of(GridResourceAttributeKeys.TAGS),
            "#", Set.of(GridResourceAttributeKeys.TOOLTIP)
        )
    );

    private static String lastSearchQuery = "";

    protected final Inventory playerInventory;

    private final GridView view;
    private final PatternRepository playerInventoryPatterns = new PatternRepositoryImpl();
    @Nullable
    private Grid grid;
    @Nullable
    private GridInsertionStrategy insertionStrategy;
    @Nullable
    private GridExtractionStrategy extractionStrategy;
    @Nullable
    private GridScrollingStrategy scrollingStrategy;
    private GridSynchronizer synchronizer;
    @Nullable
    private ResourceType resourceTypeFilter;
    private boolean active;

    protected AbstractGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final GridData gridData
    ) {
        super(menuType, syncId);

        this.playerInventory = playerInventory;

        this.active = gridData.active();

        final GridViewBuilder viewBuilder = createViewBuilder();
        gridData.resources().forEach(resource -> viewBuilder.withResource(
            resource.resourceAmount().resource(),
            resource.resourceAmount().amount(),
            resource.trackedResource().orElse(null)
        ));
        gridData.autocraftableResources().forEach(viewBuilder::withAutocraftableResource);

        this.view = viewBuilder.build();
        this.view.setSortingDirection(Platform.INSTANCE.getConfig().getGrid().getSortingDirection());
        this.view.setSortingType(Platform.INSTANCE.getConfig().getGrid().getSortingType());
        this.view.setFilterAndSort(createBaseFilter());

        this.synchronizer = loadSynchronizer();
        this.resourceTypeFilter = loadResourceType();
        this.insertionStrategy = new ClientGridInsertionStrategy();
        this.extractionStrategy = new ClientGridExtractionStrategy();
        this.scrollingStrategy = new ClientGridScrollingStrategy();
    }

    protected AbstractGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final Grid grid
    ) {
        super(menuType, syncId, playerInventory.player);

        this.view = createViewBuilder().build();

        this.playerInventory = playerInventory;
        this.grid = grid;
        this.grid.addWatcher(this, PlayerActor.class);

        this.synchronizer = NoopGridSynchronizer.INSTANCE;
        initStrategies((ServerPlayer) playerInventory.player);
    }

    private BiPredicate<GridView, GridResource> createBaseFilter() {
        return createResourceTypeFilter().and(createViewTypeFilter());
    }

    private BiPredicate<GridView, GridResource> createResourceTypeFilter() {
        return (v, resource) -> resource instanceof PlatformGridResource platformResource
            && Platform.INSTANCE.getConfig().getGrid().getResourceType().flatMap(resourceTypeId ->
            RefinedStorageApi.INSTANCE
                .getResourceTypeRegistry()
                .get(resourceTypeId)
                .map(platformResource::belongsToResourceType)
        ).orElse(true);
    }

    private BiPredicate<GridView, GridResource> createViewTypeFilter() {
        return (v, resource) -> Platform.INSTANCE.getConfig().getGrid().getViewType()
            .accepts(resource.isAutocraftable());
    }

    private static GridViewBuilder createViewBuilder() {
        return new GridViewBuilderImpl(
            new CompositeGridResourceFactory(RefinedStorageApi.INSTANCE.getResourceTypeRegistry()),
            GridSortingTypes.NAME,
            GridSortingTypes.QUANTITY
        );
    }

    public void onResourceUpdate(final ResourceKey resource,
                                 final long amount,
                                 @Nullable final TrackedResource trackedResource) {
        LOGGER.debug("{} got updated with {}", resource, amount);
        view.onChange(resource, amount, trackedResource);
    }

    public GridSortingDirection getSortingDirection() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingDirection();
    }

    public void setSortingDirection(final GridSortingDirection sortingDirection) {
        Platform.INSTANCE.getConfig().getGrid().setSortingDirection(sortingDirection);
        view.setSortingDirection(sortingDirection);
        view.sort();
    }

    public GridSortingTypes getSortingType() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingType();
    }

    public void setSortingType(final GridSortingTypes sortingType) {
        Platform.INSTANCE.getConfig().getGrid().setSortingType(sortingType);
        view.setSortingType(sortingType);
        view.sort();
    }

    public GridViewType getViewType() {
        return Platform.INSTANCE.getConfig().getGrid().getViewType();
    }

    public void setViewType(final GridViewType viewType) {
        Platform.INSTANCE.getConfig().getGrid().setViewType(viewType);
        view.sort();
    }

    public void setSearchBox(final GridSearchBox searchBox) {
        searchBox.addListener(text -> {
            final boolean valid = onSearchTextChanged(text);
            searchBox.setValid(valid);
        });
        if (Platform.INSTANCE.getConfig().getGrid().isRememberSearchQuery()) {
            searchBox.setValue(lastSearchQuery);
            searchBox.addListener(AbstractGridContainerMenu::updateLastSearchQuery);
        }
    }

    private boolean onSearchTextChanged(final String text) {
        try {
            view.setFilterAndSort(QUERY_PARSER.parse(text).and(createBaseFilter()));
            return true;
        } catch (GridQueryParserException e) {
            view.setFilterAndSort((v, resource) -> false);
            return false;
        }
    }

    private static void updateLastSearchQuery(final String text) {
        lastSearchQuery = text;
    }

    @Override
    public void removed(final Player playerEntity) {
        super.removed(playerEntity);
        if (grid != null) {
            grid.removeWatcher(this);
        }
    }

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        resetSlots();
        addPlayerInventory(playerInventory, 8, playerInventoryY, (before, after) -> {
            final Pattern beforePattern = RefinedStorageApi.INSTANCE.getPattern(before, playerInventory.player.level())
                .orElse(null);
            final Pattern afterPattern = RefinedStorageApi.INSTANCE.getPattern(after, playerInventory.player.level())
                .orElse(null);
            if (beforePattern != null) {
                playerInventoryPatterns.remove(beforePattern);
            }
            if (afterPattern != null) {
                playerInventoryPatterns.add(afterPattern);
            }
        });
    }

    public GridView getView() {
        return view;
    }

    @Override
    public void onActiveChanged(final boolean newActive) {
        this.active = newActive;
        if (this.playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
            S2CPackets.sendGridActive(serverPlayerEntity, newActive);
        }
    }

    @Override
    public void onChanged(
        final ResourceKey resource,
        final long change,
        @Nullable final TrackedResource trackedResource
    ) {
        if (!(resource instanceof PlatformResourceKey platformResource)) {
            return;
        }
        LOGGER.debug("{} received a change of {} for {}", this, change, resource);
        S2CPackets.sendGridUpdate(
            (ServerPlayer) playerInventory.player,
            platformResource,
            change,
            trackedResource
        );
    }

    @Override
    public void invalidate() {
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            initStrategies(serverPlayer);
            S2CPackets.sendGridClear(serverPlayer);
        }
    }

    private void initStrategies(final ServerPlayer player) {
        this.insertionStrategy = RefinedStorageApi.INSTANCE.createGridInsertionStrategy(
            this,
            player,
            requireNonNull(grid)
        );
        this.extractionStrategy = RefinedStorageApi.INSTANCE.createGridExtractionStrategy(
            this,
            player,
            requireNonNull(grid)
        );
        this.scrollingStrategy = RefinedStorageApi.INSTANCE.createGridScrollingStrategy(
            this,
            player,
            requireNonNull(grid)
        );
    }

    public boolean isActive() {
        return active;
    }

    private GridSynchronizer loadSynchronizer() {
        return Platform.INSTANCE
            .getConfig()
            .getGrid()
            .getSynchronizer()
            .flatMap(id -> RefinedStorageApi.INSTANCE.getGridSynchronizerRegistry().get(id))
            .orElse(NoopGridSynchronizer.INSTANCE);
    }

    @Nullable
    private ResourceType loadResourceType() {
        return Platform.INSTANCE
            .getConfig()
            .getGrid()
            .getResourceType()
            .flatMap(id -> RefinedStorageApi.INSTANCE.getResourceTypeRegistry().get(id))
            .orElse(null);
    }

    public GridSynchronizer getSynchronizer() {
        return synchronizer;
    }

    @Nullable
    public ResourceType getResourceType() {
        return resourceTypeFilter;
    }

    public void toggleSynchronizer() {
        final PlatformRegistry<GridSynchronizer> registry = RefinedStorageApi.INSTANCE.getGridSynchronizerRegistry();
        final Config.GridEntry config = Platform.INSTANCE.getConfig().getGrid();
        final GridSynchronizer newSynchronizer = registry.nextOrNullIfLast(getSynchronizer());
        if (newSynchronizer == null) {
            config.clearSynchronizer();
        } else {
            registry.getId(newSynchronizer).ifPresent(config::setSynchronizer);
        }
        this.synchronizer = newSynchronizer == null ? NoopGridSynchronizer.INSTANCE : newSynchronizer;
    }

    public void toggleResourceType() {
        final PlatformRegistry<ResourceType> registry = RefinedStorageApi.INSTANCE.getResourceTypeRegistry();
        final Config.GridEntry config = Platform.INSTANCE.getConfig().getGrid();
        final ResourceType newResourceType = resourceTypeFilter == null
            ? ResourceTypes.ITEM
            : registry.nextOrNullIfLast(resourceTypeFilter);
        if (newResourceType == null) {
            config.clearResourceType();
        } else {
            registry.getId(newResourceType).ifPresent(config::setResourceType);
        }
        this.resourceTypeFilter = newResourceType;
        this.view.sort();
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        if (insertionStrategy == null) {
            return false;
        }
        return insertionStrategy.onInsert(insertMode, tryAlternatives);
    }

    @Override
    public boolean onExtract(final PlatformResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        if (extractionStrategy == null) {
            return false;
        }
        return extractionStrategy.onExtract(resource, extractMode, cursor);
    }

    @Override
    public boolean onScroll(final PlatformResourceKey resource, final GridScrollMode scrollMode, final int slotIndex) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        if (scrollingStrategy == null) {
            return false;
        }
        return scrollingStrategy.onScroll(resource, scrollMode, slotIndex);
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("resource")
    @Override
    public ItemStack quickMoveStack(final Player playerEntity, final int slotIndex) {
        if (transferManager.transfer(slotIndex)) {
            return ItemStack.EMPTY;
        } else if (!playerEntity.level().isClientSide() && grid != null && grid.isGridActive()) {
            final Slot slot = getSlot(slotIndex);
            if (slot.hasItem() && insertionStrategy != null && canTransferSlot(slot)) {
                insertionStrategy.onTransfer(slot.index);
            }
        }
        return ItemStack.EMPTY;
    }

    protected boolean canTransferSlot(final Slot slot) {
        return true;
    }

    public void onClear() {
        view.clear();
    }

    @Nullable
    public final AutocraftableResourceHint getAutocraftableResourceHint(final Slot slot) {
        final ResourceKey resource = getResourceForAutocraftableHint(slot);
        if (resource == null) {
            return null;
        }
        return getAutocraftableResourceHint(resource);
    }

    @Nullable
    private AutocraftableResourceHint getAutocraftableResourceHint(final ResourceKey resource) {
        if (view.isAutocraftable(resource)) {
            return AutocraftableResourceHint.AUTOCRAFTABLE;
        }
        if (playerInventoryPatterns.getOutputs().contains(resource)) {
            return AutocraftableResourceHint.PATTERN_IN_INVENTORY;
        }
        return null;
    }

    @Nullable
    protected ResourceKey getResourceForAutocraftableHint(final Slot slot) {
        return null;
    }

    @Override
    public Optional<Preview> getPreview(final ResourceKey resource, final long amount) {
        return requireNonNull(grid).getPreview(resource, amount);
    }

    @Override
    public long getMaxAmount(final ResourceKey resource) {
        return requireNonNull(grid).getMaxAmount(resource);
    }

    @Override
    public boolean startTask(final ResourceKey resource, final long amount) {
        return requireNonNull(grid).startTask(resource, amount);
    }

    public boolean isLargeSlot(final Slot slot) {
        return false;
    }
}
