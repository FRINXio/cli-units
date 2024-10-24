/*
 * Copyright © 2019 Frinx and others.
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
package io.frinx.cli.unit.junos.unit.acl.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ServiceToPortMapping {

    public static final Map<String, Short> ICMP_MAPPING;
    public static final Map<String, Short> ICMPV6_MAPPING;

    private ServiceToPortMapping() {

    }

    static {

        Map<String, Short> icmpMap = new HashMap<>();
        icmpMap.put("echo-reply", (short) 0);
        icmpMap.put("unreachable", (short) 3);
        icmpMap.put("source-quench", (short) 4);
        icmpMap.put("redirect", (short) 5);
        icmpMap.put("echo-request", (short) 8);
        icmpMap.put("router-advertisement", (short) 9);
        icmpMap.put("router-solicit", (short) 10);
        icmpMap.put("time-exceeded", (short) 11);
        icmpMap.put("parameter-problem", (short) 12);
        icmpMap.put("timestamp", (short) 13);
        icmpMap.put("timestamp-reply", (short) 14);
        icmpMap.put("info-request", (short) 15);
        icmpMap.put("info-reply", (short) 16);
        icmpMap.put("mask-request", (short) 17);
        icmpMap.put("mask-reply", (short) 18);
        ICMP_MAPPING = Collections.unmodifiableMap(icmpMap);

        Map<String, Short> icmpv6Map = new HashMap<>();
        icmpv6Map.put("destination-unreachable", (short) 1);
        icmpv6Map.put("packet-too-big", (short) 2);
        icmpv6Map.put("time-exceeded", (short) 3);
        icmpv6Map.put("parameter-problem", (short) 4);
        icmpv6Map.put("private-experimentation-100", (short) 100);
        icmpv6Map.put("private-experimentation-101", (short) 101);
        icmpv6Map.put("echo-request", (short) 128);
        icmpv6Map.put("echo-reply", (short) 129);
        icmpv6Map.put("membership-query", (short) 130);
        icmpv6Map.put("membership-report", (short) 131);
        icmpv6Map.put("membership-termination", (short) 132);
        icmpv6Map.put("router-solicit", (short) 133);
        icmpv6Map.put("router-advertisement", (short) 134);
        icmpv6Map.put("neighbor-solicit", (short) 135);
        icmpv6Map.put("neighbor-advertisement", (short) 136);
        icmpv6Map.put("redirect", (short) 137);
        icmpv6Map.put("router-renumbering", (short) 138);
        icmpv6Map.put("node-information-request", (short) 139);
        icmpv6Map.put("node-information-reply", (short) 140);
        icmpv6Map.put("inverse-neighbor-discovery-solicitation", (short) 141);
        icmpv6Map.put("inverse-neighbor-discovery-advertisement", (short) 142);
        icmpv6Map.put("home-agent-address-discovery-request", (short) 144);
        icmpv6Map.put("home-agent-address-discovery-reply", (short) 145);
        icmpv6Map.put("mobile-prefix-solicitation", (short) 146);
        icmpv6Map.put("mobile-prefix-advertisement-reply", (short) 147);
        icmpv6Map.put("certificate-path-solicitation", (short) 148);
        icmpv6Map.put("certificate-path-advertisement", (short) 149);
        icmpv6Map.put("private-experimentation-200", (short) 200);
        icmpv6Map.put("private-experimentation-201", (short) 201);
        ICMPV6_MAPPING = Collections.unmodifiableMap(icmpv6Map);
    }
}
