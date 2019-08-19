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

package io.frinx.cli.unit.iosxr.ifc;

import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader;
import java.util.Collections;
import java.util.Set;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
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
        if (name.startsWith("FastEther") || name.startsWith("GigabitEthernet") || name.startsWith("TenGigE")) {
            return EthernetCsmacd.class;
        }  else if (name.startsWith("Loopback")) {
            return SoftwareLoopback.class;
        } else if (name.startsWith("Bundle-Ether")) {
            return Ieee8023adLag.class;
        } else {
            return Other.class;
        }
    }

    public static String getSubinterfaceName(InstanceIdentifier<?> id) {
        InterfaceKey ifcKey = id.firstKeyOf(Interface.class);
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);
        return getSubinterfaceName(ifcKey.getName(), subKey.getIndex());
    }

    public static String getSubinterfaceName(String ifcName, Long subId) {
        return ifcName + SubinterfaceReader.SEPARATOR + subId.toString();
    }

    public static boolean isPhysicalInterface(Config data) {
        return isPhysicalInterface(data.getType());
    }

    public static boolean isPhysicalInterface(Class<? extends InterfaceType> type) {
        return PHYS_IFC_TYPES.contains(type);
    }
}
