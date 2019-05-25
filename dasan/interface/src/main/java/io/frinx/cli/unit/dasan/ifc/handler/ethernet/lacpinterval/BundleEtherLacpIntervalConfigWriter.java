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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpinterval;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriterFormatter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherLacpIntervalConfigWriter implements CliWriterFormatter<Config>, CompositeWriter.Child<Config> {

    private final Cli cli;

    public BundleEtherLacpIntervalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        LacpEthConfigAug cfg1 = dataAfter.getAugmentation(LacpEthConfigAug.class);

        if (cfg1 == null
                || cfg1.getInterval() == null
                || !cfg1.getInterval().equals(LacpPeriodType.FAST)) { //only support FAST know
            return false;
        }

        blockingWriteAndRead(cli, id, dataAfter, "configure terminal", "bridge",
                f("lacp port timeout %s short", ifcName.replace("Ethernet", "")), "end");
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        LacpEthConfigAug cfg1 = dataBefore.getAugmentation(LacpEthConfigAug.class);

        if (cfg1 == null || cfg1.getInterval() == null) {
            return false;
        }

        blockingDeleteAndRead(cli, id, "configure terminal", "bridge",
                f("no lacp port timeout %s", ifcName.replace("Ethernet", "")), "end");
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        return writeCurrentAttributesWResult(id, dataAfter, writeContext);
    }
}
