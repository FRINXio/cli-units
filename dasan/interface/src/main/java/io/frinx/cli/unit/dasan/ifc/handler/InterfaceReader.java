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
package io.frinx.cli.unit.dasan.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader extends CompositeListReader<Interface, InterfaceKey, InterfaceBuilder>
        implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    @SuppressWarnings("serial")
    public InterfaceReader(Cli cli) {
        super(new ArrayList<CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder>>() {
            {
                add(new PhysicalPortInterfaceReader(cli));
                add(new TrunkPortInterfaceReader(cli));
                add(new BundleEtherInterfaceReader(cli));
                add(new VlanInterfaceReader(cli));
            }
        });
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Nonnull
    @Override
    public InterfaceBuilder getBuilder(@Nonnull InstanceIdentifier<Interface> instanceIdentifier) {
        return new InterfaceBuilder();
    }

    public static Class<? extends InterfaceType> parseTypeByName(String name) {

        if (name.startsWith(PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PREFIX)) {
            return EthernetCsmacd.class;
        }
        if (name.startsWith(VlanInterfaceReader.INTERFACE_NAME_PREFIX)) {
            return L3ipvlan.class;
        }
        if (name.startsWith(BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PREFIX)
            || name.startsWith(TrunkPortInterfaceReader.TRUNK_IF_NAME_PREFIX)) {
            return Ieee8023adLag.class;
        }
        return Other.class;
    }
}