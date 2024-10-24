/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.l2.cft;

import java.util.HashMap;

public final class Util {

    private Util() {
    }

    public static int getDispositionValue(String dispositionName) {
        HashMap<String, Integer> disposition = new HashMap<>();
        disposition.put("discard", 0);
        disposition.put("forward", 1);
        disposition.put("egress-l2pttranslate", 2);
        return disposition.get(dispositionName);
    }

    public static int getProtocolValue(String protocolName) {
        HashMap<String, Integer> name = new HashMap<>();
        name.put("802.1x", 0);
        name.put("all-bridges-block", 1);
        name.put("bridge-rsvd-0B0F", 2);
        name.put("bridge-rsvd-0C0D", 3);
        name.put("cisco-cdp", 4);
        name.put("cisco-dtp", 5);
        name.put("cisco-pagp", 6);
        name.put("cisco-pvst", 7);
        name.put("cisco-stp-uplink-fast", 8);
        name.put("cisco-udld", 9);
        name.put("cisco-vtp", 10);
        name.put("elmi", 11);
        name.put("esmc", 12);
        name.put("garp-block", 13);
        name.put("gmrp", 14);
        name.put("gvrp", 15);
        name.put("lacp", 16);
        name.put("lacp-marker", 17);
        name.put("lldp", 18);
        name.put("mef-ce2-bridge-block", 19);
        name.put("oam", 20);
        name.put("ptp-peer-delay", 21);
        name.put("vlan-bridge", 22);
        name.put("xstp", 23);
        name.put("bridge-block", 24);
        return name.get(protocolName);
    }
}
