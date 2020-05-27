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
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public SubPortConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            Long index = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();
            String output = blockingRead(SubPortReader.SHOW_COMMAND, cli, instanceIdentifier, readContext);
            parseSubPortConfig(output, configBuilder, parentPort, index);
        }
    }

    @VisibleForTesting
    void parseSubPortConfig(String output, ConfigBuilder builder, String parentPort, Long index) {
        Saos8SubIfNameAugBuilder augBuilder = new Saos8SubIfNameAugBuilder();

        builder.setIndex(index);
        getSubPortName(output, parentPort, index).ifPresent(augBuilder::setSubinterfaceName);

        builder.addAugmentation(Saos8SubIfNameAug.class, augBuilder.build());
    }

    @VisibleForTesting
    static Optional<String> getSubPortName(String output, String parentPort, Long index) {
        Pattern pattern = Pattern.compile(".* sub-port (?<name>\\S+) parent-port " + parentPort
                + " classifier-precedence " + index + ".*");

        return ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("name"));
    }

    private boolean isPort(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.getAllIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}