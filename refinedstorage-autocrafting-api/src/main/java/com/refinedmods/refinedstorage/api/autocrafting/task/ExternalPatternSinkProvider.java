package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;

import java.util.List;

import org.apiguardian.api.API;

/**
 * Provides access to the {@link ExternalPatternSink} for a {@link PatternLayout}.
 * Used in autocrafting tasks.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
@FunctionalInterface
public interface ExternalPatternSinkProvider {
    /**
     * @param patternLayout the pattern layout
     * @return a list of sinks for a pattern
     */
    List<ExternalPatternSink> getSinksByPatternLayout(PatternLayout patternLayout);
}
