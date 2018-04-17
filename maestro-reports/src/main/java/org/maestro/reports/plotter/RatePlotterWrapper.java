/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.reports.plotter;

import org.maestro.plotter.common.BasicPlotter;
import org.maestro.plotter.rate.DefaultRateReader;
import org.maestro.plotter.rate.RateDataProcessor;
import org.maestro.plotter.rate.graph.RatePlotter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RatePlotterWrapper implements PlotterWrapper {
    private static final Logger logger = LoggerFactory.getLogger(RatePlotterWrapper.class);

    private final DefaultRateReader rateReader = new DefaultRateReader(new RateDataProcessor());

    @Override
    public boolean plot(final File file) {
        logger.debug("Plotting Maestro compressed file {}", file.getPath());

        BasicPlotter<DefaultRateReader, RatePlotter> basicPlotter = new BasicPlotter<>(rateReader, new RatePlotter());

        try {
            basicPlotter.plot(file, file.getParentFile());
        } catch (Exception e) {
            logger.error("Unable to plot file {}: {}", file.getPath(), e.getMessage(), e);
            return false;
        }

        return true;
    }
}
