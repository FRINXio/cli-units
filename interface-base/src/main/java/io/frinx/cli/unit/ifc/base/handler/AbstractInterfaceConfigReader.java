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

package io.frinx.cli.unit.ifc.base.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.util.NetUtils;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public AbstractInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                      @NotNull final ConfigBuilder builder,
                                      @NotNull final ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        parseInterface(blockingRead(getReadCommand(ifcName), cli, id, ctx), builder, ifcName);
    }

    protected abstract String getReadCommand(String ifcName);

    @VisibleForTesting
    public void parseInterface(final String output, final ConfigBuilder builder, String name) {
        parseEnabled(output, builder);
        builder.setName(name);
        builder.setType(parseType(name));

        ParsingUtils.parseField(output,
            getMtuLine()::matcher,
            matcher -> Integer.valueOf(matcher.group("mtu")),
            builder::setMtu);

        setDescription(output, builder);
    }

    private void setDescription(String output, ConfigBuilder builder) {
        Optional<String> desc = ParsingUtils.parseField(output, 0,
            getDescriptionLine()::matcher,
            matcher -> matcher.group("desc"));

        if (desc.isPresent()) {
            if (desc.get().substring(0, 1).equals("\"")) {
                builder.setDescription(desc.get().substring(1, desc.get().length() - 1));
            } else {
                builder.setDescription(desc.get());
            }
        }
    }

    protected void parseEnabled(final String output, final ConfigBuilder builder) {
        // Set enabled unless proven otherwise
        builder.setEnabled(true);

        // Actually check if disabled
        ParsingUtils.parseField(output, 0,
            getShutdownLine()::matcher,
            matcher -> false,
            builder::setEnabled);
    }

    protected abstract Pattern getShutdownLine();

    protected Pattern getMtuLine() {
        return NetUtils.NO_MATCH;
    }

    protected abstract Pattern getDescriptionLine();

    public abstract Class<? extends InterfaceType> parseType(String name);
}