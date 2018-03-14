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

package io.frinx.cli.unit.brocade.ifc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public final class InterfaceConfigWriter implements CliWriter<Config> {

    static final String WRITE_LOOP_INTERFACE = "configure terminal\n" +
            "interface loopback {$name}\n" +
            "port-name {$data.description}\n" +
            "{% if($enabled == TRUE) %}enable\n{% else %}disable\n{% endif %}" +
            "end";

    static final String UPDATE_PHYSICAL_INTERFACE = "configure terminal\n" +
            "interface {$dataType} {$ifc}\n" +
            "{% if($data.description) %}port-name {$data.description}\n{% endif %}" +
            "{% if($data.mtu) %}mtu {$data.mtu}\n{% endif %}" +
            "{% if($enabled == TRUE) %}enable\n{% else %}disable\n{% endif %}" +
            "end";

    static final String DELETE_LOOPBACK_INT = "configure terminal\n" +
            "no interface loopback {$name}\n" +
            "end";

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (data.getType() == SoftwareLoopback.class) {
            writeLoopbackInterface(id, data, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Cannot create interface of type: " + data.getType()));
        }
    }

    private static final Pattern LOOPBACK_NAME_PATTERN = Pattern.compile("Loopback(?<number>[0-9]+)");

    private void writeLoopbackInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        Matcher matcher = LOOPBACK_NAME_PATTERN.matcher(data.getName());
        try {
            checkArgument(matcher.matches(),
                    "Loopback name must be in format: Loopback45, not: %s", data.getName());
        } catch (RuntimeException e) {
            throw new WriteFailedException.CreateFailedException(id, data, e);
        }

        blockingWriteAndRead(cli, id, data, fT(WRITE_LOOP_INTERFACE,
                "name", matcher.group("number"),
                "data", data,
                "enabled", data.isEnabled()));

    }

    public static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES = Collections.singleton(EthernetCsmacd.class);

    public static boolean isPhysicalInterface(Config data) {
        return PHYS_IFC_TYPES.contains(data.getType());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                    "Changing interface type is not permitted. Before: %s, After: %s",
                    dataBefore.getType(), dataAfter.getType());
        } catch (RuntimeException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }

        if (isPhysicalInterface(dataAfter)) {
            updatePhysicalInterface(id, dataAfter, writeContext);
        } else if (dataAfter.getType() == SoftwareLoopback.class) {
            writeLoopbackInterface(id, dataAfter, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, dataAfter,
                    new IllegalArgumentException("Unknown interface type: " + dataAfter.getType()));
        }
    }

    private void updatePhysicalInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        String ifcNumber = InterfaceConfigReader.getIfcNumber(data.getName());

        blockingWriteAndRead(cli, id, data, fT(UPDATE_PHYSICAL_INTERFACE,
                "dataType", InterfaceConfigReader.getTypeOnDevice(data.getType()),
                "ifc", ifcNumber,
                "data", data,
                "enabled", data.isEnabled()));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(dataBefore)) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        } else if (dataBefore.getType() == SoftwareLoopback.class) {
            deleteLoopbackInterface(id, dataBefore, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, dataBefore,
                    new IllegalArgumentException("Unknown interface type: " + dataBefore.getType()));
        }
    }

    private void deleteLoopbackInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {
        Matcher matcher = LOOPBACK_NAME_PATTERN.matcher(data.getName());
        try {
            checkArgument(matcher.matches(),
                    "Loopback name must be in format: Loopback45, not: %s", data.getName());
        } catch (RuntimeException e) {
            throw new WriteFailedException.DeleteFailedException(id, e);
        }

        blockingDeleteAndRead(cli, id, fT(DELETE_LOOPBACK_INT,
                "name", matcher.group("number")));
    }
}
