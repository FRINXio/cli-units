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

package io.frinx.cli.unit.iosxr.lacp.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.ethernet.EthernetConfigReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SHOW_ALL_INTERFACES = "show running-config interface | include ^interface";
    private static final String SHOW_ALL_BUNDLE_IDS = "show running-config interface | include ^ *bundle id [0-9]+.*";
    static final Pattern INTERFACE_LINE = Pattern.compile("\\s*interface (?<id>[\\S]+).*");

    private final Cli cli;

    public BundleReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        final String listOfInterfaces = blockingRead(SHOW_ALL_INTERFACES, cli, instanceIdentifier, readContext);
        final String listOfBundleIds = blockingRead(SHOW_ALL_BUNDLE_IDS, cli, instanceIdentifier, readContext);
        return parseBundleIds(listOfInterfaces, listOfBundleIds);
    }

    List<InterfaceKey> parseBundleIds(@Nonnull String listOfInterfaces, @Nonnull String listOfBundleIds) {
        final Set<InterfaceKey> interfaceKeysFromBundleInterfaces = parseIdsFromBundleInterfaces(listOfInterfaces);
        final Set<InterfaceKey> interfaceKeysFromInterfacesConfig = parseIdsFromInterfaceConfiguration(listOfBundleIds);
        final Set<InterfaceKey> union = new HashSet<>(interfaceKeysFromBundleInterfaces);
        union.addAll(interfaceKeysFromInterfacesConfig);
        return new ArrayList<>(union);
    }

    private Set<InterfaceKey> parseIdsFromBundleInterfaces(String commandOutput) {
        return ParsingUtils.parseFields(
                commandOutput,
                0,
            INTERFACE_LINE::matcher,
            matcher -> matcher.group("id"),
            String::new
        ).stream()
                .filter(new AggregateConfigReader(cli)::isLAGInterface)
                .map(InterfaceKey::new)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Set<InterfaceKey> parseIdsFromInterfaceConfiguration(String commandOutput) {
        return new HashSet<>(ParsingUtils.parseFields(
                commandOutput,
                0,
            MemberConfigReader.LACP_BUNDLE_AND_MODE_LINE::matcher,
                matcher -> matcher.group("id"),
                id -> new InterfaceKey(EthernetConfigReader.AGGREGATE_IFC_NAME + id)
        ));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder, @Nonnull ReadContext readContext) {
        interfaceBuilder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}
