/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.handlers;

import java.util.regex.Pattern;

public final class NetUtils {

    private static final Pattern DOT = Pattern.compile("\\.");

    public static final Pattern NO_MATCH = Pattern.compile("0^");

    private NetUtils() {
    }

    public static Short prefixFromNetmask(String netMask) {
        int prefixLength = DOT.splitAsStream(netMask)
                .map(Integer::parseInt)
                .map(Integer::toBinaryString)
                .map(octet -> octet.replaceAll("0", "").length())
                .mapToInt(Integer::intValue)
                .sum();

        return Integer.valueOf(prefixLength).shortValue();
    }

}
