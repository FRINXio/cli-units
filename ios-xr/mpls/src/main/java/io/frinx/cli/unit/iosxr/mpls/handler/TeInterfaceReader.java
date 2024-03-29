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

package io.frinx.cli.unit.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TeInterfaceReader implements CliConfigListReader<Interface, InterfaceKey,
        InterfaceBuilder> {

    private Cli cli;

    private static final String SHOW_RUN_MPLS_INT = "show running-config mpls traffic-eng | include ^ interface";
    // TODO Reuse pattern from ifc translate unit
    private static final Pattern INTF_LINE = Pattern.compile("interface (?<id>\\S+)");

    public TeInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                               @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SHOW_RUN_MPLS_INT, cli, instanceIdentifier, readContext);
        return getInterfaceKeys(output);
    }

    @VisibleForTesting
    static List<InterfaceKey> getInterfaceKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
                INTF_LINE::matcher,
            matcher -> matcher.group("id"),
            v -> new InterfaceKey(new InterfaceId(v)));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier, @NotNull
            InterfaceBuilder interfaceBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        InterfaceKey key = instanceIdentifier.firstKeyOf(Interface.class);
        interfaceBuilder.setInterfaceId(key.getInterfaceId());
    }
}