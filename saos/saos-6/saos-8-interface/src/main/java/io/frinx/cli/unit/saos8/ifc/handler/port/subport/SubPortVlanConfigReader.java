/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortVlanConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"classifier-precedence %d\"";

    private Cli cli;

    public SubPortVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            Long index = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();
            String output = blockingRead(f(SHOW_COMMAND, index), cli, instanceIdentifier, readContext);

            parseVlanConfig(output, configBuilder, parentPort);
        }
    }

    @VisibleForTesting
    void parseVlanConfig(String output, ConfigBuilder builder, String parentPort) {
        Saos8VlanLogicalAugBuilder augBuilder = new Saos8VlanLogicalAugBuilder();

        setIngressTransform(output, augBuilder, parentPort);
        setEgressTransform(output, augBuilder, parentPort);

        if (augBuilder.getIngressL2Transform() != null || augBuilder.getEgressL2Transform() != null) {
            builder.addAugmentation(Saos8VlanLogicalAug.class, augBuilder.build());
        }
    }

    private void setIngressTransform(String output, Saos8VlanLogicalAugBuilder builder,
                                     String parentPort) {
        Pattern ingressPattern = Pattern.compile(".* parent-port " + parentPort
                + " .* ingress-l2-transform (?<ingress>\\S+).*");

        ParsingUtils.parseField(output, 0,
            ingressPattern::matcher,
            matcher -> matcher.group("ingress"),
            builder::setIngressL2Transform);
    }

    private void setEgressTransform(String output, Saos8VlanLogicalAugBuilder builder,
                                    String parentPort) {
        Pattern egressPattern = Pattern.compile(".* parent-port " + parentPort
                + " .* egress-l2-transform (?<egress>\\S+).*");

        ParsingUtils.parseField(output, 0,
            egressPattern::matcher,
            matcher -> matcher.group("egress"),
            builder::setEgressL2Transform);
    }

    private boolean isPort(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.getAllIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}