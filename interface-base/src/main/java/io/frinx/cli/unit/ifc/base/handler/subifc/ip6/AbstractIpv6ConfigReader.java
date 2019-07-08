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

package io.frinx.cli.unit.ifc.base.handler.subifc.ip6;

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
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractIpv6ConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    protected static final short DEFAULT_PREFIX_LENGHT = (short) 64;

    private Cli cli;

    public AbstractIpv6ConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            Ipv6AddressNoZone address = instanceIdentifier.firstKeyOf(Address.class).getIp();
            parseAddressConfig(configBuilder,
                    blockingRead(getReadCommand(ifcName), cli, instanceIdentifier, readContext), address);
        }
    }

    @VisibleForTesting
    public void parseAddressConfig(ConfigBuilder configBuilder, String output, Ipv6AddressNoZone address) {
        configBuilder.setIp(address);
        configBuilder.setPrefixLength(DEFAULT_PREFIX_LENGHT);

        ParsingUtils.parseField(output,
            getIpLine()::matcher,
            m -> new Ipv6AddressNoZone(m.group("ip")),
            configBuilder::setIp);

        ParsingUtils.parseField(output,
            getIpLine()::matcher,
            m -> NetUtils.prefixFrom(m.group("prefix")),
            configBuilder::setPrefixLength);
    }

    protected abstract Pattern getIpLine();

    protected abstract String getReadCommand(String ifcName);

    public boolean hasIpAddress(InstanceIdentifier instanceIdentifier, String ifcName, ReadContext ctx)
            throws ReadFailedException {
        String output = blockingRead(getReadCommand(ifcName), cli, instanceIdentifier, ctx);
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(getIpLine()::matcher)
                .anyMatch(Matcher::matches);
    }
}
