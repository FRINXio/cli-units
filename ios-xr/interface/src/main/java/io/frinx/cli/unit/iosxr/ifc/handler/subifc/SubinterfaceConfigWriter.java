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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;
import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.getSubinterfaceName;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    private static final Set<Class<? extends InterfaceType>> SUPPORTED_INTERFACE_TYPES = Sets.newHashSet();
    static {
        SUPPORTED_INTERFACE_TYPES.add(Ieee8023adLag.class);
        SUPPORTED_INTERFACE_TYPES.add(EthernetCsmacd.class);
    }

    public SubinterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        InstanceIdentifier<Interface> parentIfcId = RWUtils.cutId(id, Interface.class);

        // If we are creating new interface, allow empty config for .0 subifc
        if (!writeContext.readBefore(parentIfcId).isPresent()
                && id.firstKeyOf(Subinterface.class).getIndex() == ZERO_SUBINTERFACE_ID) {
            Preconditions.checkArgument(data.getDescription() == null,
                    "'description' cannot be specified for .0 subinterface. " +
                            "Use 'description' under interface/config instead.");
            Preconditions.checkArgument(data.isEnabled() == null,
                    "'enabled' cannot be specified for .0 subinterface. " +
                            "Use 'enabled' under interface/config instead.");
            return;
        }

        if (isZeroSubinterface(id)) {
            // do nothing for .0 subinterface because it represents physical interface which config is handled in interface/config
            return;
        }

        Class<? extends InterfaceType> parentIfcType = writeContext.readAfter(parentIfcId).get().getConfig().getType();

        if(isSupportedType(parentIfcType)) {
            blockingWriteAndRead(cli, id, data,
                    f("interface %s", getSubinterfaceName(id)),
                    data.getDescription() == null ? "no description" : f("description %s", data.getDescription()),
                    data.isEnabled() != null && data.isEnabled() ? "no shutdown" : "shutdown",
                    "root");
        } else {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Unable to create subinterface for interface of type: " + parentIfcType));
        }
    }

    private static boolean isZeroSubinterface(@Nonnull InstanceIdentifier<?> id) throws WriteFailedException {
        Long subifcIndex = id.firstKeyOf(Subinterface.class).getIndex();
        return subifcIndex == ZERO_SUBINTERFACE_ID;

    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config data,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        InstanceIdentifier<Interface> parentIfcId = RWUtils.cutId(id, Interface.class);

        // if deleting parent interface allow deleting also .0 subifc
        if (!writeContext.readAfter(parentIfcId).isPresent()
                && id.firstKeyOf(Subinterface.class).getIndex() == ZERO_SUBINTERFACE_ID) {
            return;
        }

        if (isZeroSubinterface(id)) {
            // do nothing for .0 subinterface because it represents physical interface which config is handled in interface/config
            return;
        }

        Class<? extends InterfaceType> parentIfcType = writeContext.readBefore(parentIfcId).get().getConfig().getType();

        if(isSupportedType(parentIfcType)) {
            blockingDeleteAndRead(cli, id,
                    f("no interface %s", getSubinterfaceName(id)));
        } else {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Unable to create subinterface for interface of type: " + parentIfcType));
        }
    }

    private static boolean isSupportedType(Class<? extends InterfaceType> parentType) {
        return SUPPORTED_INTERFACE_TYPES.contains(parentType);
    }
}
