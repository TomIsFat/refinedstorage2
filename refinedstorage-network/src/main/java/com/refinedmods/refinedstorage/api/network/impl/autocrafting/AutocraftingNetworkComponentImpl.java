package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl;
import com.refinedmods.refinedstorage.api.autocrafting.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewCraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusProvider;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternListener;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocraftingNetworkComponentImpl implements AutocraftingNetworkComponent, ParentContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutocraftingNetworkComponentImpl.class);

    private final Supplier<RootStorage> rootStorageProvider;
    private final TaskStatusProvider taskStatusProvider;
    private final ExecutorService executorService;
    private final Set<PatternProvider> providers = new HashSet<>();
    private final Set<PatternListener> listeners = new HashSet<>();
    private final PatternRepositoryImpl patternRepository = new PatternRepositoryImpl();

    public AutocraftingNetworkComponentImpl(final Supplier<RootStorage> rootStorageProvider,
                                            final TaskStatusProvider taskStatusProvider,
                                            final ExecutorService executorService) {
        this.rootStorageProvider = rootStorageProvider;
        this.taskStatusProvider = taskStatusProvider;
        this.executorService = executorService;
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof PatternProvider provider) {
            provider.onAddedIntoContainer(this);
            providers.add(provider);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof PatternProvider provider) {
            provider.onRemovedFromContainer(this);
            providers.remove(provider);
        }
    }

    @Override
    public void add(final Pattern pattern) {
        patternRepository.add(pattern);
        listeners.forEach(listener -> listener.onAdded(pattern));
    }

    @Override
    public void remove(final Pattern pattern) {
        listeners.forEach(listener -> listener.onRemoved(pattern));
        patternRepository.remove(pattern);
    }

    @Override
    public Set<ResourceKey> getOutputs() {
        return patternRepository.getOutputs();
    }

    @Override
    public boolean contains(final AutocraftingNetworkComponent component) {
        return providers.stream().anyMatch(provider -> provider.contains(component));
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount) {
        return CompletableFuture.supplyAsync(() -> {
            final RootStorage rootStorage = rootStorageProvider.get();
            final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);
            final Preview preview = PreviewCraftingCalculatorListener.calculatePreview(calculator, resource, amount);
            return Optional.of(preview);
        }, executorService);
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource) {
        return CompletableFuture.supplyAsync(() -> {
            final RootStorage rootStorage = rootStorageProvider.get();
            final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);
            return calculator.getMaxAmount(resource);
        }, executorService);
    }

    @Override
    public boolean startTask(final ResourceKey resource, final long amount, final Actor actor, final boolean notify) {
        LOGGER.info("{} started a task for {}x {} with notify={}", actor, amount, resource, notify);
        return true;
    }

    @Override
    public void addListener(final PatternListener listener) {
        listeners.add(listener);
    }

    @Override
    public void addListener(final TaskStatusListener listener) {
        taskStatusProvider.addListener(listener);
    }

    @Override
    public void removeListener(final PatternListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeListener(final TaskStatusListener listener) {
        taskStatusProvider.removeListener(listener);
    }

    @Override
    public Set<Pattern> getPatterns() {
        return patternRepository.getAll();
    }

    @Override
    public List<TaskStatus> getStatuses() {
        return taskStatusProvider.getStatuses();
    }

    @Override
    public void cancel(final TaskId taskId) {
        taskStatusProvider.cancel(taskId);
    }

    @Override
    public void cancelAll() {
        taskStatusProvider.cancelAll();
    }

    @Override
    public void testUpdate() {
        taskStatusProvider.testUpdate();
    }
}
