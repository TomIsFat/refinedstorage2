package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskSnapshot;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

final class TaskSnapshotPersistence {
    private TaskSnapshotPersistence() {
    }

    static CompoundTag encodeSnapshot(final TaskSnapshot snapshot) {
        final CompoundTag tag = new CompoundTag();
        tag.putUUID("id", snapshot.id().id());
        final CompoundTag resourceTag = encodeResource(snapshot.resource());
        tag.put("resource", resourceTag);
        tag.putLong("amount", snapshot.amount());
        if (snapshot.actor() instanceof PlayerActor(String name)) {
            tag.putString("actor", name);
        }
        tag.putBoolean("notifyActor", snapshot.notifyActor());
        tag.putLong("startTime", snapshot.startTime());
        tag.put("initialRequirements", encodeResourceList(snapshot.initialRequirements()));
        tag.put("internalStorage", encodeResourceList(snapshot.internalStorage()));
        tag.putBoolean("cancelled", snapshot.cancelled());
        tag.putString("state", snapshot.state().name());
        final ListTag completedPatterns = new ListTag();
        snapshot.completedPatterns().forEach(pattern -> completedPatterns.add(encodePatternSnapshot(pattern)));
        tag.put("completedPatterns", completedPatterns);
        tag.put("patterns", encodePatternMap(snapshot.patterns()));
        return tag;
    }

    private static ListTag encodeResourceList(final ResourceList list) {
        final ListTag listTag = new ListTag();
        list.getAll().forEach(resource -> {
            final CompoundTag entryTag = encodeResource(resource);
            entryTag.putLong("amount", list.get(resource));
            listTag.add(entryTag);
        });
        return listTag;
    }

    private static CompoundTag encodePattern(final Pattern pattern) {
        final CompoundTag tag = new CompoundTag();
        tag.putUUID("id", pattern.id());
        final ListTag ingredients = new ListTag();
        for (final Ingredient ingredient : pattern.layout().ingredients()) {
            ingredients.add(encodeIngredient(ingredient));
        }
        tag.put("ingredients", ingredients);
        final ListTag outputs = new ListTag();
        for (final ResourceAmount output : pattern.layout().outputs()) {
            outputs.add(ResourceCodecs.AMOUNT_CODEC.encode(output, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        }
        tag.put("outputs", outputs);
        tag.putString("type", pattern.layout().type().name());
        return tag;
    }

    private static CompoundTag encodePatternSnapshot(final TaskSnapshot.PatternSnapshot snapshot) {
        final CompoundTag tag = new CompoundTag();
        tag.putBoolean("root", snapshot.root());
        tag.put("pattern", encodePattern(snapshot.pattern()));
        tag.put("ingredients", encodeIngredientMap(snapshot.ingredients()));
        final boolean internal = snapshot.internalPattern() != null;
        tag.putBoolean("internal", internal);
        if (snapshot.internalPattern() != null) {
            tag.put("internalPattern", encodeInternalPattern(snapshot.internalPattern()));
        } else if (snapshot.externalPattern() != null) {
            tag.put("externalPattern", encodeExternalPattern(snapshot.externalPattern()));
        }
        return tag;
    }

    private static ListTag encodePatternMap(final Map<Pattern, TaskSnapshot.PatternSnapshot> patterns) {
        final ListTag patternMap = new ListTag();
        for (final var pattern : patterns.entrySet()) {
            final CompoundTag patternTag = new CompoundTag();
            patternTag.put("k", encodePattern(pattern.getKey()));
            patternTag.put("v", encodePatternSnapshot(pattern.getValue()));
            patternMap.add(patternTag);
        }
        return patternMap;
    }

    private static CompoundTag encodeInternalPattern(final TaskSnapshot.InternalPatternSnapshot internalPattern) {
        final CompoundTag tag = new CompoundTag();
        tag.putLong("originalIterationsRemaining", internalPattern.originalIterationsRemaining());
        tag.putLong("iterationsRemaining", internalPattern.iterationsRemaining());
        return tag;
    }

    private static CompoundTag encodeExternalPattern(final TaskSnapshot.ExternalPatternSnapshot externalPattern) {
        final CompoundTag tag = new CompoundTag();
        tag.put("expectedOutputs", encodeResourceList(externalPattern.expectedOutputs()));
        tag.put("simulatedIterationInputs", encodeResourceList(externalPattern.simulatedIterationInputs()));
        tag.putLong("originalIterationsToSendToSink", externalPattern.originalIterationsToSendToSink());
        tag.putLong("iterationsToSendToSink", externalPattern.iterationsToSendToSink());
        tag.putLong("iterationsReceived", externalPattern.iterationsReceived());
        tag.putBoolean("interceptedAnythingSinceLastStep", externalPattern.interceptedAnythingSinceLastStep());
        if (externalPattern.lastSinkResult() != null) {
            tag.putString("lastSinkResult", externalPattern.lastSinkResult().name());
        }
        final ExternalPatternSinkKey lastSinkResultKey = externalPattern.lastSinkResultKey();
        if (lastSinkResultKey instanceof InWorldExternalPatternSinkKey(String name, ItemStack stack)) {
            tag.putString("lastSinkResultKeyName", name);
            tag.put("lastSinkResultKeyStack", ItemStack.CODEC
                .encode(stack, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        }
        return tag;
    }

    private static ListTag encodeIngredientMap(final Map<Integer, Map<ResourceKey, Long>> ingredients) {
        final ListTag ingredientMap = new ListTag();
        for (final var ingredient : ingredients.entrySet()) {
            final CompoundTag ingredientTag = new CompoundTag();
            ingredientTag.putInt("i", ingredient.getKey());
            ingredientTag.put("v", encodeIngredientResources(ingredient));
            ingredientMap.add(ingredientTag);
        }
        return ingredientMap;
    }

    private static ListTag encodeIngredientResources(final Map.Entry<Integer, Map<ResourceKey, Long>> ingredient) {
        final ListTag ingredientResources = new ListTag();
        for (final var resourceAndAmount : ingredient.getValue().entrySet()) {
            final CompoundTag tag = encodeResource(resourceAndAmount.getKey());
            tag.putLong("amount", resourceAndAmount.getValue());
            ingredientResources.add(tag);
        }
        return ingredientResources;
    }

    private static CompoundTag encodeResource(final ResourceKey resource) {
        return (CompoundTag) ResourceCodecs.CODEC.encode((PlatformResourceKey) resource, NbtOps.INSTANCE,
            new CompoundTag()).getOrThrow();
    }

    private static CompoundTag encodeIngredient(final Ingredient ingredient) {
        final CompoundTag ingredientTag = new CompoundTag();
        ingredientTag.putLong("amount", ingredient.amount());
        final ListTag inputsTag = new ListTag();
        for (final ResourceKey input : ingredient.inputs()) {
            inputsTag.add(encodeResource(input));
        }
        ingredientTag.put("inputs", inputsTag);
        return ingredientTag;
    }

    static TaskSnapshot decodeSnapshot(final CompoundTag tag) {
        final UUID id = tag.getUUID("id");
        final ResourceKey resource = decodeResource(tag.getCompound("resource"));
        final long amount = tag.getLong("amount");
        final Actor actor = tag.contains("actor", Tag.TAG_STRING)
            ? new PlayerActor(tag.getString("actor"))
            : Actor.EMPTY;
        final boolean notifyActor = tag.getBoolean("notifyActor");
        final long startTime = tag.getLong("startTime");
        final ResourceList initialRequirements = decodeResourceList(
            tag.getList("initialRequirements", Tag.TAG_COMPOUND)
        );
        final ResourceList internalStorage = decodeResourceList(
            tag.getList("internalStorage", Tag.TAG_COMPOUND)
        );
        final boolean cancelled = tag.getBoolean("cancelled");
        final TaskState state = TaskState.valueOf(tag.getString("state"));
        final List<TaskSnapshot.PatternSnapshot> completedPatterns = new ArrayList<>();
        for (final Tag completedTag : tag.getList("completedPatterns", Tag.TAG_COMPOUND)) {
            completedPatterns.add(decodePatternSnapshot((CompoundTag) completedTag));
        }
        final var patterns = decodePatternMap(tag.getList("patterns", Tag.TAG_COMPOUND));
        return new TaskSnapshot(
            new TaskId(id),
            resource,
            amount,
            actor,
            notifyActor,
            startTime,
            patterns,
            completedPatterns,
            initialRequirements,
            internalStorage,
            state,
            cancelled
        );
    }

    private static ResourceList decodeResourceList(final ListTag listTag) {
        final MutableResourceList resourceList = MutableResourceListImpl.create();
        for (final Tag tag : listTag) {
            final CompoundTag entryTag = (CompoundTag) tag;
            final ResourceKey resource = decodeResource(entryTag);
            final long amount = entryTag.getLong("amount");
            resourceList.add(resource, amount);
        }
        return resourceList;
    }

    private static ResourceKey decodeResource(final CompoundTag resourceTag) {
        return ResourceCodecs.CODEC.parse(NbtOps.INSTANCE, resourceTag).result().orElseThrow();
    }

    private static TaskSnapshot.PatternSnapshot decodePatternSnapshot(final CompoundTag tag) {
        final boolean root = tag.getBoolean("root");
        final Pattern pattern = decodePattern(tag.getCompound("pattern"));
        final var ingredients = decodeIngredientMap(tag.getList("ingredients", Tag.TAG_COMPOUND));
        if (tag.getBoolean("internal")) {
            final TaskSnapshot.InternalPatternSnapshot internalPattern = decodeInternalPattern(
                tag.getCompound("internalPattern")
            );
            return new TaskSnapshot.PatternSnapshot(root, pattern, ingredients, internalPattern, null);
        }
        final TaskSnapshot.ExternalPatternSnapshot externalPattern = decodeExternalPattern(
            tag.getCompound("externalPattern")
        );
        return new TaskSnapshot.PatternSnapshot(root, pattern, ingredients, null, externalPattern);
    }

    private static Pattern decodePattern(final CompoundTag tag) {
        final UUID id = tag.getUUID("id");
        final List<Ingredient> ingredients = new ArrayList<>();
        for (final Tag ingredientTag : tag.getList("ingredients", Tag.TAG_COMPOUND)) {
            ingredients.add(decodeIngredient((CompoundTag) ingredientTag));
        }
        final List<ResourceAmount> outputs = new ArrayList<>();
        for (final Tag outputTag : tag.getList("outputs", Tag.TAG_COMPOUND)) {
            outputs.add(ResourceCodecs.AMOUNT_CODEC.parse(NbtOps.INSTANCE, outputTag).result().orElseThrow());
        }
        final PatternType type = PatternType.valueOf(tag.getString("type"));
        return new Pattern(id, new PatternLayout(ingredients, outputs, type));
    }

    private static Ingredient decodeIngredient(final CompoundTag tag) {
        final long amount = tag.getLong("amount");
        final List<ResourceKey> inputs = new ArrayList<>();
        for (final Tag inputTag : tag.getList("inputs", Tag.TAG_COMPOUND)) {
            inputs.add(decodeResource((CompoundTag) inputTag));
        }
        return new Ingredient(amount, inputs);
    }

    private static TaskSnapshot.InternalPatternSnapshot decodeInternalPattern(final CompoundTag tag) {
        final long originalIterationsRemaining = tag.getLong("originalIterationsRemaining");
        final long iterationsRemaining = tag.getLong("iterationsRemaining");
        return new TaskSnapshot.InternalPatternSnapshot(originalIterationsRemaining, iterationsRemaining);
    }

    private static TaskSnapshot.ExternalPatternSnapshot decodeExternalPattern(final CompoundTag tag) {
        final ResourceList expectedOutputs = decodeResourceList(tag.getList("expectedOutputs", Tag.TAG_COMPOUND));
        final ResourceList simulatedIterationInputs =
            decodeResourceList(tag.getList("simulatedIterationInputs", Tag.TAG_COMPOUND));
        final long originalIterationsToSendToSink = tag.getLong("originalIterationsToSendToSink");
        final long iterationsToSendToSink = tag.getLong("iterationsToSendToSink");
        final long iterationsReceived = tag.getLong("iterationsReceived");
        final boolean interceptedAnythingSinceLastStep = tag.getBoolean("interceptedAnythingSinceLastStep");
        final ExternalPatternSink.Result lastSinkResult = tag.contains("lastSinkResult", Tag.TAG_STRING)
            ? ExternalPatternSink.Result.valueOf(tag.getString("lastSinkResult"))
            : null;
        final ExternalPatternSinkKey lastSinkResultKey = tag.contains("lastSinkResultKeyName", Tag.TAG_STRING)
            ? decodeSinkResultKey(tag)
            : null;
        return new TaskSnapshot.ExternalPatternSnapshot(
            expectedOutputs,
            simulatedIterationInputs,
            originalIterationsToSendToSink,
            iterationsToSendToSink,
            iterationsReceived,
            interceptedAnythingSinceLastStep,
            lastSinkResult,
            lastSinkResultKey
        );
    }

    private static InWorldExternalPatternSinkKey decodeSinkResultKey(final CompoundTag tag) {
        return new InWorldExternalPatternSinkKey(
            tag.getString("lastSinkResultKeyName"),
            ItemStack.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("lastSinkResultKeyStack")).result().orElseThrow()
        );
    }

    private static Map<Pattern, TaskSnapshot.PatternSnapshot> decodePatternMap(final ListTag patternMapTag) {
        final Map<Pattern, TaskSnapshot.PatternSnapshot> patternMap = new LinkedHashMap<>();
        for (final Tag tag : patternMapTag) {
            final CompoundTag entry = (CompoundTag) tag;
            final Pattern key = decodePattern(entry.getCompound("k"));
            final TaskSnapshot.PatternSnapshot value = decodePatternSnapshot(entry.getCompound("v"));
            patternMap.put(key, value);
        }
        return patternMap;
    }

    private static Map<Integer, Map<ResourceKey, Long>> decodeIngredientMap(final ListTag ingredientMapTag) {
        final Map<Integer, Map<ResourceKey, Long>> ingredients = new LinkedHashMap<>();
        for (final Tag tag : ingredientMapTag) {
            final CompoundTag entry = (CompoundTag) tag;
            final int index = entry.getInt("i");
            final Map<ResourceKey, Long> resources = decodeIngredientResources(entry.getList("v", Tag.TAG_COMPOUND));
            ingredients.put(index, resources);
        }
        return ingredients;
    }

    private static Map<ResourceKey, Long> decodeIngredientResources(final ListTag ingredientResources) {
        final Map<ResourceKey, Long> resources = new LinkedHashMap<>();
        for (final Tag rawTag : ingredientResources) {
            final CompoundTag tag = (CompoundTag) rawTag;
            final ResourceKey resource = decodeResource(tag);
            final long amount = tag.getLong("amount");
            resources.put(resource, amount);
        }
        return resources;
    }
}
