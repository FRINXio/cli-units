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

package io.frinx.cli.unit.ios.ifc;

import com.google.common.collect.HashBiMap;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader;
import java.util.Collections;
import java.util.Set;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724.SPEEDAUTO;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ETHERNETSPEED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED100GB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED100MB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED10GB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED10MB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED1GB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED25GB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED40GB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED50GB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEEDUNKNOWN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Bridge;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class Util {

    private static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES =
            Collections.singleton(EthernetCsmacd.class);

    private Util() {

    }

    public static Class<? extends InterfaceType> parseType(String name) {
        if (name.startsWith("FastEther") || name.contains("GigabitEthernet")) {
            return EthernetCsmacd.class;
        } else if (name.startsWith("Port-channel")) {
            return Ieee8023adLag.class;
        } else if (name.startsWith("Loopback")) {
            return SoftwareLoopback.class;
        } else if (name.startsWith("BDI")) {
            return Bridge.class;
        } else {
            return Other.class;
        }
    }

    public static String getSubinterfaceName(InstanceIdentifier<?> id) {
        InterfaceKey ifcKey = id.firstKeyOf(Interface.class);
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);
        return ifcKey.getName() + SubinterfaceReader.SEPARATOR + subKey.getIndex().toString();
    }

    public static boolean isPhysicalInterface(Config data) {
        return isPhysicalInterface(data.getType());
    }

    public static boolean isPhysicalInterface(Class<? extends InterfaceType> type) {
        return PHYS_IFC_TYPES.contains(type);
    }

    public static boolean canSetInterfaceSpeed(String interfaceName) {
        final Class<? extends InterfaceType> type = parseType(interfaceName);
        return type.equals(EthernetCsmacd.class) || type.equals(Ieee8023adLag.class);
    }

    public static Class<? extends ETHERNETSPEED> parseSpeed(String name) {
        Class<? extends ETHERNETSPEED> ethernetSpeed = getBiMap().get(name);
        return ethernetSpeed == null ? SPEEDUNKNOWN.class : ethernetSpeed;
    }

    public static String getSpeedName(Class<? extends ETHERNETSPEED> ethernetSpeed) {
        return ethernetSpeed == SPEEDUNKNOWN.class ? "auto" : getBiMap().inverse().get(ethernetSpeed);
    }

    private static HashBiMap<String, Class<? extends ETHERNETSPEED>> getBiMap() {
        HashBiMap<String, Class<? extends ETHERNETSPEED>> biMap = HashBiMap.create();
        biMap.put("10", SPEED10MB.class);
        biMap.put("100", SPEED100MB.class);
        biMap.put("1000", SPEED1GB.class);
        biMap.put("10000", SPEED10GB.class);
        biMap.put("25000", SPEED25GB.class);
        biMap.put("40000", SPEED40GB.class);
        biMap.put("50000", SPEED50GB.class);
        biMap.put("100000", SPEED100GB.class);
        biMap.put("auto", SPEEDAUTO.class);
        return biMap;
    }

    public static StormControl.Address getStormControlAddress(String name) {
        switch (name) {
            case "broadcast":
                return StormControl.Address.Broadcast;
            case "multicast":
                return StormControl.Address.Multicast;
            case "unicast":
                return StormControl.Address.Unicast;
            default:
                return null;
        }
    }

}
