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

package io.frinx.cli.unit.brocade.network.instance.l2p2p.ifc;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.unit.utils.CliListWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PInterfaceWriter implements CliListWriter<Interface, InterfaceKey>, CompositeWriter.Child<Interface> {

    @Override
    public boolean writeCurrentAttributesWResult(InstanceIdentifier<Interface> instanceIdentifier,
                                                 Interface anInterface,
                                                 WriteContext writeContext) {
        return L2P2PInterfaceReader.L2P2P_CHECK.canProcess(instanceIdentifier, writeContext, false);
    }

    @Override
    public boolean updateCurrentAttributesWResult(InstanceIdentifier<Interface> id,
                                                  Interface dataBefore,
                                                  Interface dataAfter,
                                                  WriteContext writeContext) {
        return L2P2PInterfaceReader.L2P2P_CHECK.canProcess(id, writeContext, false);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(InstanceIdentifier<Interface> instanceIdentifier,
                                                  Interface anInterface,
                                                  WriteContext writeContext) {
        return L2P2PInterfaceReader.L2P2P_CHECK.canProcess(instanceIdentifier, writeContext, true);
    }
}
