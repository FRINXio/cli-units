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

    public static final Map<String, Integer> TCP_MAPPING;
    public static final Map<String, Short> ICMP_MAPPING;
    public static final Map<String, Short> ICMPV6_MAPPING;

    private ServiceToPortMapping() {

    }

    static {
        Map<String, Integer> tcpMap = new HashMap<>();
        // taken from XR 5.3.4 using ?
        tcpMap.put("bgp", 179);
        tcpMap.put("chargen", 19);
        tcpMap.put("cmd", 514);
        tcpMap.put("daytime", 13);
        tcpMap.put("discard", 9);
        tcpMap.put("domain", 53);
        tcpMap.put("echo", 7);
        tcpMap.put("exec", 512);
        tcpMap.put("finger", 79);
        tcpMap.put("ftp", 21);
        tcpMap.put("ftp-data", 20);
        tcpMap.put("gopher", 70);
        tcpMap.put("hostname", 101);
        tcpMap.put("http", 80);
        tcpMap.put("ident", 113);
        tcpMap.put("irc", 194);
        tcpMap.put("klogin", 543);
        tcpMap.put("kshell", 544);
        tcpMap.put("ldp", 646);
        tcpMap.put("login", 513);
        tcpMap.put("lpd", 515);
        tcpMap.put("nntp", 119);
        tcpMap.put("pim-auto-rp", 496);
        tcpMap.put("pop2", 109);
        tcpMap.put("pop3", 110);
        tcpMap.put("smtp", 25);
        tcpMap.put("ssh", 22);
        tcpMap.put("sunrpc", 111);
        tcpMap.put("tacacs", 49);
        tcpMap.put("talk", 517);
        tcpMap.put("telnet", 23);
        tcpMap.put("time", 37);
        tcpMap.put("uucp", 540);
        tcpMap.put("whois", 43);
        tcpMap.put("www", 80);
        TCP_MAPPING = Collections.unmodifiableMap(tcpMap);

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
