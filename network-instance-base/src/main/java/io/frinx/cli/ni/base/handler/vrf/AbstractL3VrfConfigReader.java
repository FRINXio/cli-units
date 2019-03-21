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

package io.frinx.cli.ni.base.handler.vrf;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractL3VrfConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private AbstractL3VrfReader parentReader;

    private Cli cli;

    protected AbstractL3VrfConfigReader(AbstractL3VrfReader parentReader, Cli cli) {
        this.parentReader = parentReader;
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
            @Nonnull InstanceIdentifier<Config> instanceIdentifier,
            @Nonnull ConfigBuilder configBuilder,
            @Nonnull ReadContext readContext) throws ReadFailedException {
        String niName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (parentReader.isL3Vrf(instanceIdentifier.firstKeyOf(NetworkInstance.class),
                instanceIdentifier, readContext)) {
            configBuilder.setName(niName);
            configBuilder.setType(L3VRF.class);
        }
        parseVrfConfig(blockingRead(getReadCommand(), cli, instanceIdentifier, readContext), configBuilder);
    }

    protected abstract String getReadCommand();

    @VisibleForTesting
    public void parseVrfConfig(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output,
            getRouteDistinguisherLine()::matcher,
            matcher -> matcher.group("rd"),
            rd -> builder.setRouteDistinguisher(new RouteDistinguisher(rd)));

        ParsingUtils.parseField(output,
            getDescriptionLine()::matcher,
            matcher -> matcher.group("desc"),
            builder::setDescription);

        // TODO set other attributes
    }

    protected abstract Pattern getRouteDistinguisherLine();

    protected abstract Pattern getDescriptionLine();
}
