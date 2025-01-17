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
package io.ceresdb.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.ceresdb.common.Tenant;

/**
 * A tool class for generating auth headers.
 *
 * @author jiachun.fjc
 */
public class AuthUtil {

    public static final String HEAD_TIMESTAMP           = "x-ceresdb-timestamp";
    public static final String HEAD_ACCESS_TENANT       = "x-ceresdb-access-tenant";
    public static final String HEAD_ACCESS_TOKEN        = "x-ceresdb-access-token";
    public static final String HEAD_ACCESS_CHILD_TENANT = "x-ceresdb-access-child-tenant";

    public static Map<String, String> authHeaders(final Tenant tenant) {
        if (tenant == null) {
            return Collections.emptyMap();
        }

        final String timestamp = String.valueOf(Clock.defaultClock().getTick());
        final String token = tenant.getToken();
        final String tenantName = tenant.getTenant();
        final String childTenantName = tenant.getChildTenant();

        final Map<String, String> headers = new HashMap<>();
        headers.put(HEAD_TIMESTAMP, timestamp);
        if (tenantName != null) {
            headers.put(HEAD_ACCESS_TENANT, tenantName);
        }
        if (token != null) {
            headers.put(HEAD_ACCESS_TOKEN, SHA256(token + timestamp));
        }
        if (childTenantName != null) {
            headers.put(HEAD_ACCESS_CHILD_TENANT, childTenantName);
        }
        return headers;
    }

    public static void replaceChildTenant(final Map<String, String> headers, final String childTenant) {
        if (childTenant != null) {
            headers.put(HEAD_ACCESS_CHILD_TENANT, childTenant);
        }
    }

    private static String SHA256(final String str) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(final byte[] bytes) {
        final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
        final StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (final byte b : bytes) {
            buf.append(HEX_DIGITS[(b >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[b & 0x0f]);
        }
        return buf.toString();
    }
}
