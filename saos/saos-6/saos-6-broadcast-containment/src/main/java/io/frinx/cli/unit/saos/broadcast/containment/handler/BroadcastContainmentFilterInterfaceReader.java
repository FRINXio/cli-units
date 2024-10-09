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

package io.frinx.cli.unit.saos.broadcast.containment.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.Filter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentFilterInterfaceReader
        implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final Pattern BC_FILTER_PORTS_LINE_PATTERN = Pattern.compile(".*port (?<ports>.*)");

    private Cli cli;

    public BroadcastContainmentFilterInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        String filterName = instanceIdentifier.firstKeyOf(Filter.class).getName();
        return getAllIds(cli, this, filterName, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    static List<InterfaceKey> getAllIds(Cli cli, CliReader cliReader, String filterName,
                                     @NotNull InstanceIdentifier<?> id,
                                     @NotNull ReadContext readContext) throws ReadFailedException {
        String outputForFilter = "configuration search string \"broadcast-containment add filter "
                + filterName + "\"";
        String output = cliReader.blockingRead(outputForFilter, cli, id, readContext);
        List<String> interfaceKeys = new ArrayList<>();
        ParsingUtils.parseFields(output, 0,
            BC_FILTER_PORTS_LINE_PATTERN::matcher,
            m -> m.group("ports"),
            interfaceKeys::add);

        return interfaceKeys.isEmpty() ? Collections.emptyList() : parsePorts(interfaceKeys);
    }

    @VisibleForTesting
    static List<InterfaceKey> parsePorts(List<String> interfaceKeys) {
        String[] ports = interfaceKeys.get(0).split(",");
        List<InterfaceKey> portkeys = new ArrayList<>();
        for (String port : ports) {
            portkeys.add(new InterfaceKey(port));
        }
        return portkeys;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder interfaceBuilder,
                                      @NotNull ReadContext readContext) {
        interfaceBuilder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}