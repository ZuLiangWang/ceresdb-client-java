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
package io.ceresdb.rpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import io.grpc.MethodDescriptor.Marshaller;

import io.ceresdb.common.util.StringBuilderHelper;

public final class StringMarshaller implements Marshaller<String> {

    public static final StringMarshaller INSTANCE = new StringMarshaller();

    @Override
    public InputStream stream(final String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String parse(final InputStream stream) {
        try {
            return toStringBuilder((new InputStreamReader(stream, StandardCharsets.UTF_8))).toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static StringBuilder toStringBuilder(final Reader r) throws IOException {
        final StringBuilder buf = StringBuilderHelper.get();
        copyReaderToBuilder(r, buf);
        return buf;
    }

    static void copyReaderToBuilder(final Reader from, final StringBuilder to) throws IOException {
        final char[] buf = new char[0x800];
        int nRead;
        while ((nRead = from.read(buf)) != -1) {
            to.append(buf, 0, nRead);
        }
    }
}
