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

package io.frinx.cli.unit.junos.ifc.handler.subifc.ip4;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.ifc.base.handler.subifc.ip4.AbstractIpv4ConfigWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.ifc.handler.subifc.SubinterfaceReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class Ipv4ConfigWriter extends AbstractIpv4ConfigWriter {

    private static final String TEMPLATE = "{% if($delete) %}delete{% else %}set{% endif %} "
            + "interfaces {$name} family inet address {$config.ip.value}/{$config.prefix_length}";

    private final Cli cli;

    public Ipv4ConfigWriter(Cli cli) {
        super(cli);
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, ifcName));
    }

    @Override
    protected String writeTemplate(Config config, String ifcName) {
        return fT(TEMPLATE, "name", junosIfcName(ifcName), "config", config);
    }

    private static String junosIfcName(String ifcName) {
        return ifcName + SubinterfaceReader.SEPARATOR + 0;
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config, deleteTemplate(config, ifcName));
    }

    @Override
    protected String deleteTemplate(Config config, String ifcName) {
        return fT(TEMPLATE, "name", junosIfcName(ifcName), "config", config, "delete", true);
    }
}
