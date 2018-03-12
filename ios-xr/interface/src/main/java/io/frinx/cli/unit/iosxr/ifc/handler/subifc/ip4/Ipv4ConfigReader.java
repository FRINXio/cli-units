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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4;

import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4.Ipv4AddressReader.INTERFACE_IP_LINE;
import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4.Ipv4AddressReader.SH_RUN_INT_IP;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4ConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern DOT = Pattern.compile("\\.");

    private Cli cli;

    public Ipv4ConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Long subId = id.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            parseAddressConfig(configBuilder, blockingRead(String.format(SH_RUN_INT_IP, name), cli, id, readContext));
        }
    }

    @VisibleForTesting
    static void parseAddressConfig(ConfigBuilder configBuilder, String output) {
        parseField(output,
                INTERFACE_IP_LINE::matcher,
                m -> new Ipv4AddressNoZone(m.group("address")),
                configBuilder::setIp);

        parseField(output,
                INTERFACE_IP_LINE::matcher,
                m -> prefixFromNetmask(m.group("prefix")),
                configBuilder::setPrefixLength);
    }

    private static Short prefixFromNetmask(String netMask) {
        int prefixLength = DOT.splitAsStream(netMask)
                .map(Integer::parseInt)
                .map(Integer::toBinaryString)
                .map(octet -> octet.replaceAll("0", "").length())
                .mapToInt(Integer::intValue)
                .sum();

        return Integer.valueOf(prefixLength).shortValue();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((AddressBuilder) builder).setConfig(config);
    }
}
