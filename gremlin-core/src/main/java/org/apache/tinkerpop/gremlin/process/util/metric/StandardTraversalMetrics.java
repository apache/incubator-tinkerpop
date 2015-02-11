/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.tinkerpop.gremlin.process.util.metric;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Bob Briody (http://bobbriody.com)
 */
public final class StandardTraversalMetrics implements TraversalMetrics, Serializable {
    // toString() specific headers
    private static final String[] HEADERS = {"Step", "Count", "Traversers", "Time (ms)", "% Dur"};

    private static final String ITEM_COUNT_DISPLAY = "item count";

    private boolean dirty = true;
    private final Map<String, MutableMetrics> metrics = new HashMap<>();
    private final Map<Integer, String> indexToLabelMap = new TreeMap<>();

    /*
    The following are computed values upon the completion of profiling in order to report the results back to the user
     */
    private long totalStepDuration;
    private Map<String, ImmutableMetrics> computedMetrics;
    private boolean isComputer = false;

    public StandardTraversalMetrics() {
    }

    public void start(final String metricsId) {
        dirty = true;
        metrics.get(metricsId).start();
    }

    public void stop(final String metricsId) {
        dirty = true;
        metrics.get(metricsId).stop();
    }

    public void finish(final String metricsId, final long bulk) {
        dirty = true;
        final MutableMetrics m = metrics.get(metricsId);
        m.finish(1);
        m.getNested(ELEMENT_COUNT_ID).incrementCount(bulk);
    }


    @Override
    public long getDuration(final TimeUnit unit) {
        computeTotals();
        return unit.convert(totalStepDuration, MutableMetrics.SOURCE_UNIT);
    }

    @Override
    public Metrics getMetrics(final int index) {
        computeTotals();
        // adjust index to account for the injected profile steps
        return (Metrics) computedMetrics.get(indexToLabelMap.get(index * 2 + 1));
    }

    @Override
    public Metrics getMetrics(final String stepLabel) {
        computeTotals();
        return computedMetrics.get(stepLabel);
    }

    @Override
    public Collection<ImmutableMetrics> getMetrics() {
        computeTotals();
        return computedMetrics.values();
    }

    @Override
    public String toString() {
        computeTotals();

        // Build a pretty table of metrics data.

        // Append headers
        final StringBuilder sb = new StringBuilder("Traversal Metrics\n")
                .append(String.format("%28s %21s %11s %15s %8s", HEADERS));

        // Append each StepMetric's row. indexToLabelMap values are ordered by index.
        for (String label : indexToLabelMap.values()) {
            final ImmutableMetrics s = computedMetrics.get(label);
            final String rowName = StringUtils.abbreviate(s.getName(), 28);
            final long itemCount = s.getNested(ELEMENT_COUNT_ID).getCount();

            sb.append(String.format("%n%28s %21d %11d %15.3f %8.2f",
                    rowName, itemCount, s.getCount(), s.getDuration(TimeUnit.MICROSECONDS) / 1000.0, s.getPercentDuration()));
        }

        // Append total duration
        sb.append(String.format("%n%28s %21s %11s %15.3f %8s",
                "TOTAL", "-", "-", getDuration(TimeUnit.MICROSECONDS) / 1000.0, "-"));

        return sb.toString();
    }

    private void computeTotals() {
        if (!dirty) {
            // already good to go
            return;
        }

        // Create temp list of ordered metrics
        List<MutableMetrics> tempMetrics = new ArrayList<>(metrics.size());
        for (String label : indexToLabelMap.values()) {
            // The indexToLabelMap is sorted by index (key)
            tempMetrics.add(metrics.get(label).clone());
        }

        if (!isComputer) {
            // Subtract upstream traversal time from each step
            for (int ii = tempMetrics.size() - 1; ii > 0; ii--) {
                MutableMetrics cur = tempMetrics.get(ii);
                MutableMetrics upStream = tempMetrics.get(ii - 1);
                cur.setDuration(cur.getDuration(MutableMetrics.SOURCE_UNIT) - upStream.getDuration(MutableMetrics.SOURCE_UNIT));
            }
        }

        // Calculate total duration
        this.totalStepDuration = 0;
        tempMetrics.forEach(m -> this.totalStepDuration += m.getDuration(MutableMetrics.SOURCE_UNIT));

        // Assign %'s
        tempMetrics.forEach(m ->
                        m.setPercentDuration(m.getDuration(TimeUnit.NANOSECONDS) * 100.d / this.totalStepDuration)
        );

        // Store immutable instances of the calculated metrics
        computedMetrics = new HashMap<>(metrics.size());
        tempMetrics.forEach(it -> computedMetrics.put(it.getId(), it.getImmutableClone()));

        dirty = false;
    }

    public static StandardTraversalMetrics merge(final Iterator<StandardTraversalMetrics> toMerge) {
        final StandardTraversalMetrics newTraversalMetrics = new StandardTraversalMetrics();

        // iterate the incoming TraversalMetrics
        toMerge.forEachRemaining(inTraversalMetrics -> {
            newTraversalMetrics.isComputer = inTraversalMetrics.isComputer;

            // aggregate the internal Metrics
            inTraversalMetrics.metrics.forEach((metricsId, toAggregate) -> {

                MutableMetrics aggregateMetrics = newTraversalMetrics.metrics.get(metricsId);
                if (null == aggregateMetrics) {
                    // need to create a Metrics to aggregate into
                    aggregateMetrics = new MutableMetrics(toAggregate.getId(), toAggregate.getName());

                    newTraversalMetrics.metrics.put(metricsId, aggregateMetrics);
                    // Set the index of the Metrics
                    for (Map.Entry<Integer, String> entry : inTraversalMetrics.indexToLabelMap.entrySet()) {
                        if (metricsId.equals(entry.getValue())) {
                            newTraversalMetrics.indexToLabelMap.put(entry.getKey(), metricsId);
                            break;
                        }
                    }
                }
                aggregateMetrics.aggregate(toAggregate);
            });
        });
        return newTraversalMetrics;
    }

    public void initializeIfNecessary(final String metricsId, final int index, final String displayName, final boolean isComputer) {
        if (indexToLabelMap.containsKey(index)) {
            return;
        }

        this.isComputer = isComputer;
        final MutableMetrics newMetrics = new MutableMetrics(metricsId, displayName);
        // Add a nested metric for item count
        newMetrics.addNested(new MutableMetrics(ELEMENT_COUNT_ID, ITEM_COUNT_DISPLAY));

        // The index is necessary to ensure that step order is preserved after a merge.
        indexToLabelMap.put(index, metricsId);
        metrics.put(metricsId, newMetrics);
    }
}
