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

package io.frinx.cli.iosxr.ospfv3.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.types.rev180817.STUBROUTERMAXMETRIC;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StubRouterConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    static final String SH_OSPF_V3_STUBRTR =
            "show running-config router ospfv3 %s %s stub-router router-lsa max-metric";

    public StubRouterConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<Config> iid, ConfigBuilder builder,
            ReadContext context) throws ReadFailedException {
        final String ospfId = iid.firstKeyOf(Protocol.class).getName();
        final String nwInsName = OspfV3ProtocolReader.resolveVrfWithName(iid);
        final String output = blockingRead(String.format(SH_OSPF_V3_STUBRTR, ospfId, nwInsName), cli, iid, context);

        if (StringUtils.isNotEmpty(output)) {
            builder.setSet(true);
            builder.setAdvertiseLsasTypes(STUBROUTERMAXMETRIC.class); //only support max-metric now
            if (StringUtils.contains(output, "always")) {
                builder.setAlways(true);
            }
        }
    }
}
