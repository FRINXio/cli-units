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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class Util {

    private static final Pattern IFC_NAME = Pattern.compile("[a-z]+\\s+(?<number>[^a-zA-Z]+)");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES =
            Collections.singleton(EthernetCsmacd.class);

    private Util() {
    }

    public static Class<? extends InterfaceType> parseType(String name) {
        return IFC_TYPE_MAP.entrySet().stream()
                .filter(entry -> name.startsWith(entry.getValue()))
                .findFirst()
                .<Class<? extends InterfaceType>>map(Map.Entry::getKey).orElse(Other.class);
    }

    public static String getIfcNumber(String name) {
        Matcher matcher = IFC_NAME.matcher(name);
        Preconditions.checkArgument(matcher.matches(), "Interface name %s in unexpected format. Expected format: "
                + "ethernet 1/10", name);
        return matcher.group("number");
    }

    public static String expandInterfaceName(String name) {
        String expandedName = WHITESPACE_PATTERN.splitAsStream(name)
                .findFirst()
                .flatMap(shortName -> IFC_TYPE_MAP.values().stream()
                        .filter(fullName -> fullName.startsWith(shortName))
                        .findFirst())
                .orElseThrow(() -> new IllegalArgumentException("Unable to expand interface name from " + name
                        + ", available interface types: " + IFC_TYPE_MAP));

        return expandedName + " " + getIfcNumber(name);
    }

    private static final Map<Class<? extends InterfaceType>, String> IFC_TYPE_MAP = new HashMap<>();

    static {
        IFC_TYPE_MAP.put(EthernetCsmacd.class, "ethernet");
        IFC_TYPE_MAP.put(SoftwareLoopback.class, "loopback");
        IFC_TYPE_MAP.put(L3ipvlan.class, "ve");
    }

    public static String getTypeOnDevice(Class<? extends InterfaceType> openconfigType) {
        return IFC_TYPE_MAP.getOrDefault(openconfigType, "other");
    }

    public static boolean isPhysicalInterface(Config data) {
        return PHYS_IFC_TYPES.contains(data.getType());
    }
}
