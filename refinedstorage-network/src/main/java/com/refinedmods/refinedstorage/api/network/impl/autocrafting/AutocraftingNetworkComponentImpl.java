package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewCraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskImpl;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternListener;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlanCraftingCalculatorListener.calculatePlan;

public class AutocraftingNetworkComponentImpl implements AutocraftingNetworkComponent, ParentContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutocraftingNetworkComponentImpl.class);

    private final Supplier<RootStorage> rootStorageProvider;
    private final ExecutorService executorService;
    private final Set<PatternProvider> providers = new HashSet<>();
    private final Map<Pattern, PatternProvider> providerByPattern = new HashMap<>();
    private final Set<PatternListener> listeners = new HashSet<>();
    private final PatternRepositoryImpl patternRepository = new PatternRepositoryImpl();

    public AutocraftingNetworkComponentImpl(final Supplier<RootStorage> rootStorageProvider,
                                            final ExecutorService executorService) {
        this.rootStorageProvider = rootStorageProvider;
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
    public CompletableFuture<Boolean> startTask(final ResourceKey resource,
                                                final long amount,
                                                final Actor actor,
                                                final boolean notify) {
        return CompletableFuture.supplyAsync(() -> {
            final RootStorage rootStorage = rootStorageProvider.get();
            final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);
            return calculatePlan(calculator, resource, amount)
                .map(plan -> startTask(resource, amount, actor, plan))
                .orElse(false);
        });
    }

    private boolean startTask(final ResourceKey resource,
                              final long amount,
                              final Actor actor,
                              final TaskPlan plan) {
        final Task task = TaskImpl.fromPlan(plan);
        LOGGER.debug("Created task {} for {}x {} for {}", task.getId(), amount, resource, actor);
        final PatternProvider patternProvider = CoreValidations.validateNotNull(
            providerByPattern.get(plan.rootPattern()),
            "No provider for pattern " + plan.rootPattern()
        );
        patternProvider.addTask(task);
        return true;
    }

    @Override
    public void addListener(final PatternListener listener) {
        listeners.add(listener);
    }

    @Override
    public void addListener(final TaskStatusListener listener) {
        // TODO(feat): autocrafting monitor
    }

    @Override
    public void removeListener(final PatternListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeListener(final TaskStatusListener listener) {
        // TODO(feat): autocrafting monitor
    }

    @Override
    public Set<Pattern> getPatterns() {
        return patternRepository.getAll();
    }

    @Override
    public List<Pattern> getPatternsByOutput(final ResourceKey output) {
        return patternRepository.getByOutput(output);
    }

    @Override
    public List<TaskStatus> getStatuses() {
        // TODO(feat): autocrafting monitor
        return List.of();
    }

    @Override
    public void cancel(final TaskId taskId) {
        // TODO(feat): autocrafting monitor
    }

    @Override
    public void cancelAll() {
        // TODO(feat): autocrafting monitor
    }

    @Override
    public void add(final PatternProvider provider, final Pattern pattern, final int priority) {
        patternRepository.add(pattern, priority);
        providerByPattern.put(pattern, provider);
        listeners.forEach(listener -> listener.onAdded(pattern));
    }

    @Override
    public void remove(final PatternProvider provider, final Pattern pattern) {
        listeners.forEach(listener -> listener.onRemoved(pattern));
        providerByPattern.remove(pattern);
        patternRepository.remove(pattern);
    }

    @Override
    public void update(final Pattern pattern, final int priority) {
        patternRepository.update(pattern, priority);
    }

    // TODO(feat): processing pattern balancing
    @Override
    public boolean accept(final Pattern pattern, final Collection<ResourceAmount> resources, final Action action) {
        final PatternProvider patternProvider = providerByPattern.get(pattern);
        if (patternProvider == null) {
            return false;
        }
        return patternProvider.accept(resources, action);
    }
}
