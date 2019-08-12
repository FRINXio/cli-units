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

package io.frinx.cli.unit.brocade.network.instance.vrf.ifc;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfInterfaceWriter implements CompositeWriter.Child<Interface>, CliListWriter<Interface, InterfaceKey> {

    private final Cli cli;
    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface %s\n"
            + "vrf forwarding %s\n"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "interface %s\n"
            + "no vrf forwarding %s\n"
            + "end";

    public VrfInterfaceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(InstanceIdentifier<Interface> instanceIdentifier,
                                                 Interface anInterface,
                                                 WriteContext writeContext) throws WriteFailedException {

        if (!VrfInterfaceReader.VRF_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            return false;
        }

        return true;

//        boolean ifcExists = writeContext.readAfter(IIDs.INTERFACES.child(org.opendaylight.yang.gen.v1.http.frinx
//                        .openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
//                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
//                        .interfaces.InterfaceKey(anInterface.getId())))
//                .isPresent();
//        Preconditions.checkArgument(ifcExists, "Interface: %s does not exist, cannot add it to VRF", anInterface
//                .getId());
//
//        final NetworkInstance networkInstance =
//                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, NetworkInstance.class))
//                        .get();
//
//        blockingWriteAndRead(cli, instanceIdentifier, anInterface,
//                f(WRITE_TEMPLATE,
//                        anInterface.getId(),
//                        networkInstance.getConfig()
//                                .getName()));
//
//        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(InstanceIdentifier<Interface> id,
                                                  Interface dataBefore,
                                                  Interface dataAfter,
                                                  WriteContext writeContext) throws
            WriteFailedException {
        return writeCurrentAttributesWResult(id, dataAfter, writeContext);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(InstanceIdentifier<Interface> instanceIdentifier,
                                                  Interface anInterface,
                                                  WriteContext writeContext) throws WriteFailedException {
        if (!VrfInterfaceReader.VRF_CHECK.canProcess(instanceIdentifier, writeContext, true)) {
            return false;
        }

        return true;

//        NetworkInstanceKey networkInstanceKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
//
//        boolean ifcExists = writeContext.readAfter(IIDs.INTERFACES.child(org.opendaylight.yang.gen.v1.http.frinx
//                        .openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
//                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
//                        .interfaces.InterfaceKey(anInterface.getId())))
//                .isPresent();
//        if (!ifcExists) {
//            // No point of removing vrf from nonexisting ifc
//            return true;
//        }
//
//        blockingDeleteAndRead(cli, instanceIdentifier,
//                f(DELETE_TEMPLATE,
//                        anInterface.getId(),
//                        networkInstanceKey.getName()));
//
//        return true;
    }
}
