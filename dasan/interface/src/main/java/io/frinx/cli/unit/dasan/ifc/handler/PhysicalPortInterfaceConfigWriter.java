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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeChildWriter;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PhysicalPortInterfaceConfigWriter implements CliWriter<Config>, CompositeChildWriter<Config> {

    public static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES =
        Collections.singleton(EthernetCsmacd.class);

    private Cli cli;

    public PhysicalPortInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return false;
        }

        Preconditions.checkArgument(isPhysicalInterface(data),
            "Unexpected interface type: %s", data.getType());

        throw new IllegalArgumentException("Cannot create physical interface");
    }

    private void writeOrUpdateInterface(InstanceIdentifier<Config> id, Config data, String portid)
            throws WriteFailedException.CreateFailedException {

        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            "bridge",
            BooleanUtils.isTrue(data.isEnabled())
                ? f("port enable %s", portid)
                    : f("port disable %s", portid),
            data.getMtu() == null
                ? f("no jumbo-frame %s", portid)
                    : f("jumbo-frame %s %s", portid, data.getMtu()),
            "end");
    }

    private static boolean isPhysicalInterface(Config data) {
        return PHYS_IFC_TYPES.contains(data.getType());
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return false;
        }

        Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
            "Changing interface type is not permitted. Before: %s, After: %s",
            dataBefore.getType(), dataAfter.getType());

        // we support update also for physical interfaces
        Preconditions.checkArgument(isPhysicalInterface(dataAfter),
            "Unexpected interface type: %s", dataAfter.getType());

        writeOrUpdateInterface(id, dataAfter, matcher.group("portid"));
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return false;
        }

        Preconditions.checkArgument(isPhysicalInterface(dataBefore),
            "Unexpected interface type: %s", dataBefore.getType());

        throw new IllegalArgumentException("Cannot delete physical interface");
    }
}
