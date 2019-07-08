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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip6.AbstractIpv6ConfigWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6ConfigWriter extends AbstractIpv6ConfigWriter {

    static final String MISSING_IP_ADDRESS_MSG = "Missing IP address";
    static final String MISSING_PREFIX_LENGTH_MSG = "Missing prefix length";

    private static final String WRITE_TEMPLATE = "interface {$name}\n"
            + "ipv6 address {$data.ip.value}/{$data.prefix_length}\n"
            + "root";

    private static final String DELETE_TEMPLATE = "interface {$name}\n"
            + "no ipv6 address\n"
            + "root";

    public Ipv6ConfigWriter(final Cli cli) {
        super(cli);
    }

    @Override
    public void writeCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config config,
                                       @Nonnull final WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();

        Preconditions.checkArgument(config.getIp() != null, MISSING_IP_ADDRESS_MSG);
        Preconditions.checkArgument(config.getPrefixLength() != null, MISSING_PREFIX_LENGTH_MSG);
        Ipv6CheckUtil.checkParentInterfaceTypeWithExeption(ifcName, EthernetCsmacd.class, Ieee8023adLag.class,
                SoftwareLoopback.class);

        super.writeCurrentAttributes(id, config, writeContext);
    }

    @Override
    protected String writeTemplate(Config config, String ifcName) {
        return fT(WRITE_TEMPLATE, "name", ifcName, "data", config);
    }

    @Override
    protected String deleteTemplate(Config config, String ifcName) {
        return fT(DELETE_TEMPLATE, "name", ifcName);
    }
}
