/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.cable.handler.cablemac;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.CableMac;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.cable.mac.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.cable.mac.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.state.extension.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.state.extension.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.state.extension.interfaces.InterfaceBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableMacStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_CABLE_MAC = "show cable modem cable-mac %s";

    private final Cli cli;

    public CableMacStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var name = id.firstKeyOf(CableMac.class).getId();
        String output = blockingRead(f(SH_CABLE_MAC, name), cli, id, readContext);
        parseCableMac(output, stateBuilder);
    }

    @VisibleForTesting
    static void parseCableMac(String output, StateBuilder stateBuilder) {
        InterfaceBuilder interfaceBuilder = new InterfaceBuilder();
        List<Interface> interfaces = new ArrayList<>();
        List<String> interfaceNames = ParsingUtils.parseNonDistinctFields(output, 0,
                CableMacReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("interfaceName"),
                Function.identity());

        List<String> bondeds = ParsingUtils.parseNonDistinctFields(output, 0,
                CableMacReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("bonded"),
                Function.identity());

        List<String> states = ParsingUtils.parseNonDistinctFields(output, 0,
                CableMacReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("state"),
                Function.identity());

        List<String> docSisList = ParsingUtils.parseNonDistinctFields(output, 0,
                CableMacReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("docSis"),
                Function.identity());

        List<String> qosList = ParsingUtils.parseNonDistinctFields(output, 0,
                CableMacReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("qos"),
                Function.identity());

        List<String> cpes = ParsingUtils.parseNonDistinctFields(output, 0,
                CableMacReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("cpe"),
                Function.identity());

        List<String> macAddresses = ParsingUtils.parseNonDistinctFields(output, 0,
                CableMacReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("macAddress"),
                Function.identity());

        List<String> ipAddresses = ParsingUtils.parseNonDistinctFields(output, 0,
                CableMacReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("ipAddress"),
                Function.identity());

        for (int i = 0; i < interfaceNames.size(); i++) {
            interfaceBuilder.setId(interfaceNames.get(i));
            interfaceBuilder.setName(interfaceNames.get(i));
            interfaceBuilder.setBonded(bondeds.get(i));
            interfaceBuilder.setState(states.get(i).trim());
            interfaceBuilder.setDocSis(docSisList.get(i));
            interfaceBuilder.setQos(qosList.get(i));
            interfaceBuilder.setCpe(cpes.get(i));
            interfaceBuilder.setMacAddress(macAddresses.get(i));
            interfaceBuilder.setIpAddress(ipAddresses.get(i));
            interfaces.add(interfaceBuilder.build());
        }

        InterfacesBuilder interfacesBuilder = new InterfacesBuilder();
        interfacesBuilder.setInterface(interfaces);

        stateBuilder.setInterfaces(interfacesBuilder.build());
    }
}