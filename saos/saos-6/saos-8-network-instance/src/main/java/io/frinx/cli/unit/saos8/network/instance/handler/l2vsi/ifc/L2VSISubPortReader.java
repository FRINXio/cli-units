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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAugBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSISubPortReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    static final String SHOW_COMMAND = "configuration search string \"vs %s\"";

    private final Cli cli;

    public L2VSISubPortReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        String vsName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        return getAllIds(cli, this, instanceIdentifier, readContext, vsName);
    }

    @VisibleForTesting
    static List<InterfaceKey> getAllIds(Cli cli, CliReader cliReader,
                                                     @Nonnull InstanceIdentifier<?> id,
                                                     @Nonnull ReadContext readContext,
                                                     String vsName) throws ReadFailedException {
        var output = cliReader.blockingRead(String.format(SHOW_COMMAND, vsName), cli, id, readContext);
        var subPortPattern = Pattern.compile("virtual-switch interface attach sub-port (?<name>\\S+)"
                + " vs " + vsName);

        return ParsingUtils.parseFields(output, 0,
            subPortPattern::matcher,
            m -> m.group("name"),
            InterfaceKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder,
                                      @Nonnull ReadContext readContext) {
        final var id = instanceIdentifier.firstKeyOf(Interface.class).getId();
        interfaceBuilder.setId(id).setConfig(new ConfigBuilder().setId(id).addAugmentation(Saos8NiIfcAug.class,
                        new Saos8NiIfcAugBuilder().setType(Ieee8023adLag.class).build()).build());
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
