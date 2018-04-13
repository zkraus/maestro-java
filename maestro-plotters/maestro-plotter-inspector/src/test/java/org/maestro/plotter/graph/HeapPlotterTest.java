/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.plotter.graph;

import org.junit.Test;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;
import org.maestro.plotter.inspector.heap.HeapData;
import org.maestro.plotter.inspector.heap.HeapProcessor;
import org.maestro.plotter.inspector.heap.HeapReader;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class HeapPlotterTest {
    @Test
    public void testPlot() throws IOException, EmptyDataSet, IncompatibleDataSet {
        String fileName = this.getClass().getResource("/data-ok/heap.csv").getPath();

        HeapProcessor heapProcessor = new HeapProcessor();
        HeapReader heapReader = new HeapReader(heapProcessor);

        HeapData heapData = heapReader.read(fileName);

        File sourceFile = new File(fileName);
        HeapPlotter plotter = new HeapPlotter();

        File outputFile = new File(sourceFile.getParentFile(), "heap_memory.png");
        plotter.plot(heapData, sourceFile.getParentFile());

        assertTrue("The output file does not exist: " + outputFile.getPath(), outputFile.exists());

    }
}
