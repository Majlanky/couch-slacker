/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groocraft.couchdb.slacker.benchmark;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;

public class HeapProfiler implements InternalProfiler {

    @Override
    public String getDescription() {
        return "Heap profiler";
    }

    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {

    }

    @Override
    public Collection<? extends Result<?>> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams,
                                                       IterationResult result) {


        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean() ;
        MemoryUsage memoryUsage = memoryBean.getHeapMemoryUsage();

        Collection<ScalarResult> results = new ArrayList<>();
        results.add(new ScalarResult("used-heap", memoryUsage.getUsed(), "bytes", AggregationPolicy.MAX));
        results.add(new ScalarResult("total-heap", memoryUsage.getCommitted(), "bytes", AggregationPolicy.MAX));

        return results;
    }
}