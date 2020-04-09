/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.l2vlan.L2VLANInterfaceReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SHOW_COMMAND =
            "configuration search string \"virtual-switch interface attach cpu-subinterface\"";
    private final Cli cli;

    public L2VSIInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isL2VLAN(instanceIdentifier, readContext)) {
            String vsName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
            String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
            return getAllIds(output, vsName);
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    static List<InterfaceKey> getAllIds(String output, String vsName) {

        Pattern vrPattern = Pattern.compile("virtual-switch interface attach cpu-subinterface (?<name>\\S+)"
                + " vs " + vsName);
        return ParsingUtils.parseFields(output, 0,
            vrPattern::matcher,
            matcher -> matcher.group("name"),
            InterfaceKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isL2VLAN(instanceIdentifier, readContext)) {
            interfaceBuilder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
        }
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    private boolean isL2VLAN(InstanceIdentifier<Interface> id, ReadContext readContext) throws ReadFailedException {
        return L2VLANInterfaceReader.getAllIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}
