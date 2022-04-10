/*
 * Copyright 2014-2022 the original author or authors.
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

import com.groocraft.couchdb.slacker.DocumentDescriptor;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import com.groocraft.couchdb.slacker.utils.LazyLog;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Some benchmark to find out {@link LazyLog} is or is not a goofy idea. I am pretty sure, that is not good because it creates new instances which are
 * collected immediately in most cases but a code is much more cleaner with this construction. When SLF4J 2.0 will be stable, I am going to remove this because
 * SLF4J in 2.0 version support lambdas as parameters (accomplish the same performance improvement without heap consuming).
 *
 * If you have a feeling I did something wrong or the solution causing troubles, please let me know.
 *
 * @author Majlanky
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Benchmark {

    private static final EntityMetadata entityMetadata = new EntityMetadata(DocumentDescriptor.of(TestDocument.class));
    private static final TestDocument testDocument = new TestDocument();

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder().include(Benchmark.class.getName()).addProfiler(HeapProfiler.class).build()).run();
    }

    @org.openjdk.jmh.annotations.Benchmark
    @Fork(warmups = 1, value = 1)
    @BenchmarkMode(Mode.AverageTime)
    public void lazy(){
       log(LazyLog.of(() -> entityMetadata.getRevisionReader().read(testDocument)));
    }

    @org.openjdk.jmh.annotations.Benchmark
    @Fork(warmups = 1, value = 1)
    @BenchmarkMode(Mode.AverageTime)
    public void check(){
        if(isLogEnabled()) {
            log(entityMetadata.getRevisionReader().read(testDocument));
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    @Fork(warmups = 1, value = 1)
    @BenchmarkMode(Mode.AverageTime)
    public void direct(){
        log(entityMetadata.getRevisionReader().read(testDocument));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void log(Object o){
        if(isLogEnabled()){
            o.toString();
        }
    }

    private boolean isLogEnabled(){
        return false;
    }

}
