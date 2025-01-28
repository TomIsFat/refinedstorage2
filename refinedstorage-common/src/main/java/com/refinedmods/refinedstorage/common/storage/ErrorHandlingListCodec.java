package com.refinedmods.refinedstorage.common.storage;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ErrorHandlingListCodec<E> implements Codec<List<E>> {
    private static final String ERROR_MESSAGE = """
        Refined Storage could not load a resource in storage.
        This could be because the resource no longer exists after a mod update, or if the data format of the
        resource has changed. In any case, this is NOT caused by Refined Storage.
        Refined Storage will try to gracefully handle this problem and continue to load the storage data.
        The problematic resource might end up being removed from storage, or may no longer have any additional data
        associated with it.
        Error message:""";

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingListCodec.class);

    private final Codec<E> elementCodec;

    ErrorHandlingListCodec(final Codec<E> elementCodec) {
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<T> encode(final List<E> input, final DynamicOps<T> ops, final T prefix) {
        final ListBuilder<T> builder = ops.listBuilder();
        for (final E element : input) {
            builder.add(elementCodec.encodeStart(ops, element));
        }
        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<List<E>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
            final DecoderState<T> decoder = new DecoderState<>(ops);
            stream.accept(decoder::accept);
            return decoder.build();
        });
    }

    @Override
    public String toString() {
        return "ErrorHandlingListCodec[" + elementCodec + ']';
    }

    private class DecoderState<T> {
        private final DynamicOps<T> ops;
        private final List<E> elements = new ArrayList<>();

        private DecoderState(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        private void accept(final T value) {
            final DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);
            elementResult.error().ifPresent(
                error -> LOGGER.warn("{} {}", ERROR_MESSAGE, error.message())
            );
            elementResult.resultOrPartial().ifPresent(pair -> elements.add(pair.getFirst()));
        }

        private DataResult<Pair<List<E>, T>> build() {
            return DataResult.success(Pair.of(elements, ops.empty()));
        }
    }
}
