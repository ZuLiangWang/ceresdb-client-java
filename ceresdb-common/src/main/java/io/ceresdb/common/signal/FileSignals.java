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

import java.io.File;
import java.nio.file.Paths;

/**
 * @author jiachun.fjc
 */
public class FileSignals {

    private static final String[] EMPTY_ARRAY = new String[0];

    public static boolean ignoreSignal(final FileSignal fileSignal) {
        return !Paths.get(FileOutputHelper.getOutDir(), fileSignal.getFilename()).toFile().exists();
    }

    public static boolean ignoreFileOutputSignal() {
        return list().length > 0;
    }

    public static String[] list() {
        final File dir = new File(FileOutputHelper.getOutDir());
        if (!dir.exists() || !dir.isDirectory()) {
            return EMPTY_ARRAY;
        }
        return dir.list((d, name) -> FileSignal.parse(name).isPresent());
    }
}
