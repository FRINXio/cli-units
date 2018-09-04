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

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public  class VlanInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> ,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    //command to get configurations of VLAN Interface
    private static final String SHOW_VLAN_INTERFACE = "show running-config | include ^interface br[1-9][0-9]*";
    private static final Pattern VLAN_INTERFACE_LINE = Pattern.compile("^interface br(?<id>[1-9][0-9]*)$");
    public static final String INTERFACE_NAME_PREFIX = "Vlan";
    public static final Pattern INTERFACE_NAME_PATTERN = Pattern.compile("Vlan(?<id>[1-9][0-9]*)$");

    private Cli cli;

    public VlanInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseAllInterfaceIds(blockingRead(SHOW_VLAN_INTERFACE, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<InterfaceKey> parseAllInterfaceIds(String output) {
        return NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(VLAN_INTERFACE_LINE::matcher)
                .filter(Matcher::matches)
                .map(m -> INTERFACE_NAME_PREFIX + m.group("id"))
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}