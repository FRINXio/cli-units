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

package io.frinx.cli.unit.brocade.ifc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class Util {

    private static final Set<String> ETHERNET_NAME_PREFIX = Sets.newHashSet(
            "GigabitEther", "10GigabitEther", "FastEther", "Ethernetmgmt");

    private static final Pattern IFC_NAME = Pattern.compile("[^a-zA-Z]*[a-zA-Z]+(?<number>[^a-zA-Z]+)");

    private static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES = Collections.singleton(EthernetCsmacd
            .class);

    private Util() {}

    public static Class<? extends InterfaceType> parseType(String name) {
        for (String ethernetNamePrefix : ETHERNET_NAME_PREFIX) {
            if (name.startsWith(ethernetNamePrefix)) {
                return EthernetCsmacd.class;
            }
        }
        if (name.startsWith("Loopback")) {
            return SoftwareLoopback.class;
        } else {
            return Other.class;
        }
    }

    public static String getIfcNumber(String name) {
        Matcher matcher = IFC_NAME.matcher(name);
        Preconditions.checkArgument(matcher.matches(), "Interface name %s in unexpected format. Expected format: "
                + "GigabitEthernet1/0", name);
        return matcher.group("number");
    }

    private static final Map<Class<? extends InterfaceType>, String> IFC_TYPE_MAP = new HashMap<>();

    static {
        IFC_TYPE_MAP.put(EthernetCsmacd.class, "ethernet");
        IFC_TYPE_MAP.put(SoftwareLoopback.class, "loopback");
    }

    public static String getTypeOnDevice(Class<? extends InterfaceType> openconfigType) {
        return IFC_TYPE_MAP.getOrDefault(openconfigType, "other");
    }

    public static boolean isPhysicalInterface(Config data) {
        return PHYS_IFC_TYPES.contains(data.getType());
    }
}
