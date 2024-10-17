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

package io.frinx.cli.unit.saos.qos;

import java.util.HashMap;

public final class Util {

    private Util() {
    }

    public static int getModeValue(String name) {
        HashMap<String, Integer> mode = new HashMap<>();
        mode.put("none", 0);
        mode.put("advanced", 1);
        mode.put("standard-dot1dpri", 2);
        mode.put("standard-ip-prec", 3);
        mode.put("standard-dscp", 4);
        mode.put("standard-vlan", 5);
        mode.put("standard-vlan-dot1dpri", 6);
        mode.put("standard-vlan-ip-prec", 7);
        mode.put("standard-vlan-dscp", 8);
        mode.put("hierarchical-port", 9);
        mode.put("hierarchical-vlan", 10);
        return mode.get(name);
    }

    public static String getModeName(int value) {
        HashMap<Integer, String> mode = new HashMap<>();
        mode.put(0, "none");
        mode.put(1, "advanced");
        mode.put(2, "standard-dot1dpri");
        mode.put(3, "standard-ip-prec");
        mode.put(4, "standard-dscp");
        mode.put(5, "standard-vlan");
        mode.put(6, "standard-vlan-dot1dpri");
        mode.put(7, "standard-vlan-ip-prec");
        mode.put(8, "standard-vlan-dscp");
        mode.put(9, "hierarchical-port");
        mode.put(10, "hierarchical-vlan");
        return mode.get(value);
    }
}
