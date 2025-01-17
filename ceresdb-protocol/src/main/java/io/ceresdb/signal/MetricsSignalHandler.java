/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ceresdb.signal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ceresdb.common.SPI;
import io.ceresdb.common.signal.FileOutputHelper;
import io.ceresdb.common.signal.FileSignals;
import io.ceresdb.common.signal.SignalHandler;
import io.ceresdb.common.util.Files;
import io.ceresdb.common.util.MetricReporter;
import io.ceresdb.common.util.MetricsUtil;

/**
 * A signal handle that can write the metrics into a file.
 *
 * @author jiachun.fjc
 */
@SPI(priority = 97)
public class MetricsSignalHandler implements SignalHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsSignalHandler.class);

    private static final String BASE_NAME = "CeresDB_client_metrics.log";

    @Override
    public void handle(final String signalName) {
        if (FileSignals.ignoreFileOutputSignal()) {
            return;
        }

        try {
            final File file = FileOutputHelper.getOutputFile(BASE_NAME);

            LOG.info("Printing CeresDB client metrics triggered by signal: {} to file: {}.", signalName,
                    file.getAbsoluteFile());

            try (PrintStream out = new PrintStream(new FileOutputStream(file, true))) {
                final MetricReporter reporter = MetricReporter.forRegistry(MetricsUtil.metricRegistry()) //
                        .outputTo(out) //
                        .prefixedWith("-- CeresDB") //
                        .build();
                reporter.report();
                out.flush();
            }
            Files.fsync(file);
        } catch (final IOException e) {
            LOG.error("Fail to print CeresDB client metrics.", e);
        }
    }
}
