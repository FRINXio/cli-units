/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ifc.base.handler.subifc.ip4;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.ifc.base.util.NetUtils;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractIpv4ConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    protected final Cli cli;

    public AbstractIpv4ConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (isSupportedInterface(instanceIdentifier)) {
            Ipv4AddressNoZone address = instanceIdentifier.firstKeyOf(Address.class).getIp();
            parseAddressConfig(configBuilder,
                    blockingRead(getReadCommand(ifcName, subId), cli, instanceIdentifier, readContext), address);
        }
    }

    @VisibleForTesting
    public void parseAddressConfig(ConfigBuilder configBuilder, String output, Ipv4AddressNoZone address) {
        configBuilder.setIp(address);

        ParsingUtils.parseField(output,
            getIpLine()::matcher,
            m -> new Ipv4AddressNoZone(m.group("ip")),
            configBuilder::setIp);

        ParsingUtils.parseField(output,
            getIpLine()::matcher,
            m -> NetUtils.prefixFrom(m.group("prefix")),
            configBuilder::setPrefixLength);
    }

    protected abstract Pattern getIpLine();

    protected abstract String getReadCommand(String ifcName, Long subId);

    @VisibleForTesting
    public boolean isSupportedInterface(InstanceIdentifier<Config> instanceIdentifier) {
        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        return AbstractSubinterfaceReader.isSubInterfaceZero(instanceIdentifier);
    }

    public boolean hasIpAddress(InstanceIdentifier instanceIdentifier, String ifcName, ReadContext ctx)
            throws ReadFailedException {
        String output = blockingRead(getReadCommand(ifcName, AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID),
                cli, instanceIdentifier, ctx);
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(getIpLine()::matcher)
                .anyMatch(Matcher::matches);
    }
}