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

package io.frinx.cli.ifc.base.handler.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractSubinterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    protected AbstractSubinterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        // Parse subifc's configuration from device only for non 0 subifc
        if (subKey.getIndex() == AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            builder.setIndex(subKey.getIndex());
            return;
        }
        String subIfcName = getSubinterfaceName(id);
        parseSubinterface(blockingRead(getReadCommand(subIfcName), cli, id, ctx),
            builder, subKey.getIndex(), subIfcName);
    }

    protected abstract String getReadCommand(String subIfcName);

    protected abstract String getSubinterfaceName(InstanceIdentifier<Config> instanceIdentifier);

    @VisibleForTesting
    public void parseSubinterface(final String output, final ConfigBuilder builder, Long subKey, String name) {
        // Set enabled unless proven otherwise
        builder.setEnabled(true);
        builder.setIndex(subKey);
        builder.setName(name);

        // Actually check if disabled
        ParsingUtils.parseField(output, 0,
            getShutdownLine()::matcher,
            matcher -> false,
            builder::setEnabled);

        ParsingUtils.parseField(output,
            getDescriptionLine()::matcher,
            matcher -> matcher.group("desc"),
            builder::setDescription);
    }

    protected abstract Pattern getShutdownLine();

    protected abstract Pattern getDescriptionLine();
}
