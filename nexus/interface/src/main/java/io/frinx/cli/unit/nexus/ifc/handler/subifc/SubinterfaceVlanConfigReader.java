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

package io.frinx.cli.unit.nexus.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceVlanConfigReader;
import io.frinx.cli.unit.nexus.ifc.Util;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceVlanConfigReader extends AbstractSubinterfaceVlanConfigReader {
    private static final Pattern VLAN_TAG_LINE = Pattern.compile("encapsulation dot1q (?<tag>[0-9]+)");
    private static final String CONFIGURATION_INTERFACES = "show running-config interface %s";

    public SubinterfaceVlanConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getSubinterfaceName(InstanceIdentifier<Config> instanceIdentifier) {
        return Util.getSubinterfaceName(instanceIdentifier);
    }

    @Override
    protected Pattern getVlanTagLine() {
        return VLAN_TAG_LINE;
    }

    @Override
    protected String getReadCommand() {
        return CONFIGURATION_INTERFACES;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        if (subKey.getIndex() == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            return;
        }
        super.readCurrentAttributes(id, builder, ctx);
    }
}