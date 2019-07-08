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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceConfigReader;
import io.frinx.cli.unit.junos.ifc.Util;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceConfigReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceConfigReader extends AbstractSubinterfaceConfigReader {

    private static final Pattern SUBIF_DESCRIPTION_LINE = Pattern
        .compile("set interfaces (?<ifcId>\\S+) unit (?<subifcIndex>[0-9]+) description (?<desc>.*)");

    private static final Pattern SUBIF_DISABLE_LINE = Pattern
            .compile("set interfaces (?<ifcId>\\S+) unit (?<subifcIndex>[0-9]+) disable");

    private Cli cli;

    public SubinterfaceConfigReader(Cli cli) {
        super(cli);
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext ctx) throws ReadFailedException {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        String subIfcName = getSubinterfaceName(id);
        parseSubinterface(blockingRead(getReadCommand(subIfcName), cli, id, ctx),
            builder, subKey.getIndex(), subIfcName);
    }

    @Override
    protected String getReadCommand(String subIfcName) {
        return f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, subIfcName);
    }

    @Override
    protected String getSubinterfaceName(InstanceIdentifier<Config> instanceIdentifier) {
        return Util.getSubinterfaceName(instanceIdentifier);
    }

    @Override
    protected Pattern getShutdownLine() {
        return SUBIF_DISABLE_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return SUBIF_DESCRIPTION_LINE;
    }
}
