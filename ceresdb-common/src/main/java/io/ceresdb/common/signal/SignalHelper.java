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
package io.ceresdb.common.signal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ceresdb.common.util.Platform;

/**
 * A signal helper, provides ANSI/ISO C signal support.
 *
 * @author jiachun.fjc
 */
public final class SignalHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SignalHelper.class);

    private static final SignalAccessor SIGNAL_ACCESSOR = getSignalAccessor0();

    public static boolean supportSignal() {
        // Do not support windows.
        return !Platform.isWindows() && SIGNAL_ACCESSOR != null;
    }

    /**
     * Registers user signal handlers.
     *
     * @param signal   signal
     * @param handlers user signal handlers
     * @return true if support on current platform
     */
    public static boolean addSignal(final Signal signal, final List<SignalHandler> handlers) {
        if (SIGNAL_ACCESSOR != null) {
            SIGNAL_ACCESSOR.addSignal(signal.signalName(), handlers);
            return true;
        }
        return false;
    }

    private static SignalAccessor getSignalAccessor0() {
        return hasSignal0() ? new SignalAccessor() : null;
    }

    private static boolean hasSignal0() {
        try {
            Class.forName("sun.misc.Signal");
            return true;
        } catch (final Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("sun.misc.Signal: unavailable.", t);
            }
        }
        return false;
    }

    private SignalHelper() {
    }

    static class SignalAccessor {

        public void addSignal(final String signalName, final List<SignalHandler> handlers) {
            final sun.misc.Signal signal = new sun.misc.Signal(signalName);
            final SignalHandlerAdapter adapter = new SignalHandlerAdapter(signal, handlers);
            sun.misc.Signal.handle(signal, adapter);
        }
    }

    static class SignalHandlerAdapter implements sun.misc.SignalHandler {

        private final sun.misc.Signal     target;
        private final List<SignalHandler> handlers;

        public static void addSignal(final SignalHandlerAdapter adapter) {
            sun.misc.Signal.handle(adapter.target, adapter);
        }

        public SignalHandlerAdapter(sun.misc.Signal target, List<SignalHandler> handlers) {
            this.target = target;
            this.handlers = handlers;
        }

        @Override
        public void handle(final sun.misc.Signal signal) {
            try {
                if (!this.target.equals(signal)) {
                    return;
                }

                LOG.info("Handling signal {}.", signal);

                for (final SignalHandler h : this.handlers) {
                    h.handle(signal.getName());
                }
            } catch (final Throwable t) {
                LOG.error("Fail to handle signal: {}.", signal, t);
            }
        }
    }
}
