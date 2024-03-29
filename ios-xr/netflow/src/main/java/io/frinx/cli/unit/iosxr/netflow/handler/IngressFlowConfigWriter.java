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

package io.frinx.cli.unit.iosxr.netflow.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.netflow.handler.util.InterfaceCheckUtil;
import io.frinx.cli.unit.iosxr.netflow.handler.util.NetflowUtils;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.ingress.flows.ingress.flow.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressFlowConfigWriter implements CliWriter<Config> {

    private static final String FLOW_CMD_WITH_SAMPLER = "flow %s monitor %s sampler %s ingress";
    private static final String FLOW_CMD_WITHOUT_SAMPLER = "flow %s monitor %s ingress";
    private static final String NO_FLOW_CMD = "no flow %s monitor %s ingress";
    private final Cli cli;

    public IngressFlowConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void writeCurrentAttributes(@NotNull final InstanceIdentifier<Config> id, @NotNull final Config dataAfter,
                                       @NotNull final WriteContext writeContext) throws WriteFailedException {
        InterfaceCheckUtil.checkInterfaceTypeWithException(id, EthernetCsmacd.class, Ieee8023adLag.class);
        final InterfaceKey interfaceKey = NetflowUtils.checkInterfaceExists(id, writeContext);

        String dampConfCommand = getNetflowCommand(dataAfter);

        String ifcName = interfaceKey.getId()
                .getValue();
        blockingWriteAndRead(cli, id, dataAfter,
                f("interface %s", ifcName),
                dampConfCommand,
                "root");
    }

    private String getNetflowCommand(final Config dataAfter) {
        final String monitorName = dataAfter.getMonitorName();
        final Class<? extends NETFLOWTYPE> netflowType = dataAfter.getNetflowType();
        final String samplerName = dataAfter.getSamplerName();

        //flow ipv6|ipv4 <monitor name> sampler <sampler name> ingress
        if (samplerName != null && !samplerName.isEmpty()) {
            return f(FLOW_CMD_WITH_SAMPLER, NetflowUtils.getNetflowStringType(netflowType), monitorName, samplerName);
        } else {
            return f(FLOW_CMD_WITHOUT_SAMPLER, NetflowUtils.getNetflowStringType(netflowType), monitorName);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull final InstanceIdentifier<Config> id, @NotNull final Config dataBefore,
                                        @NotNull final WriteContext writeContext) throws WriteFailedException {
        InterfaceCheckUtil.checkInterfaceTypeWithException(id, EthernetCsmacd.class, Ieee8023adLag.class);

        String deleteCommand = getNoNetflowCommand(dataBefore);

        String ifcName = id.firstKeyOf(Interface.class)
                .getId()
                .getValue();
        blockingDeleteAndRead(cli, id,
                f("interface %s", ifcName),
                deleteCommand,
                "root");
    }

    private String getNoNetflowCommand(final Config dataAfter) {
        final String monitorName = dataAfter.getMonitorName();
        final Class<? extends NETFLOWTYPE> netflowType = dataAfter.getNetflowType();

        return f(NO_FLOW_CMD, NetflowUtils.getNetflowStringType(netflowType), monitorName);
    }

    @Override
    public void updateCurrentAttributes(@NotNull final InstanceIdentifier<Config> id, @NotNull final Config dataBefore,
                                        @NotNull final Config dataAfter, @NotNull final WriteContext writeContext)
            throws WriteFailedException {
        // this is a MUST, when attempting direct update:

        // interface Bundle-Ether7029
        // flow ipv6 monitor IPV6_FLOWMONITOR_EXAMPLE sampler SAMPLER_EXAMPLE2 ingress
        //!!% 'nfma' detected the 'warning' condition 'Attempt to apply a duplicate flow monitor to the interface'
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }
}