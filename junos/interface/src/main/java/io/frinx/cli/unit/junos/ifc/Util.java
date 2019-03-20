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

package io.frinx.cli.unit.junos.ifc;

import com.google.common.collect.ImmutableSet;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.junos.ifc.handler.subifc.SubinterfaceReader;
import java.util.Set;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class Util {

    private static final Set<Class<? extends InterfaceType>> PHYSICAL_INTERFACE_TYPES =
            ImmutableSet.of(EthernetCsmacd.class);

    private Util() {

    }

    public static Class<? extends InterfaceType> parseType(final String name) {
        if (name.startsWith("et-") || name.startsWith("xe-") || name.startsWith("ge-") || name.startsWith("fe-")) {
            return EthernetCsmacd.class;
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
        return !PHYSICAL_INTERFACE_TYPES.contains(data.getType());
    }

    public static boolean isSubinterface(InterfaceKey ifcName) {
        return InterfaceReader.SUBINTERFACE_NAME.matcher(ifcName.getName()).matches();
    }
}
