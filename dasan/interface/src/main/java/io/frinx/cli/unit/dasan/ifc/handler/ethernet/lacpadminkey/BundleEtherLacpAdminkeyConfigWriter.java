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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpadminkey;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherLacpAdminkeyConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public BundleEtherLacpAdminkeyConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        Config1 adminkeyAug = dataAfter.getAugmentation(Config1.class);

        if (adminkeyAug == null || adminkeyAug.getAdminKey() == null) {
            return;
        }
        blockingWriteAndRead(cli, id, dataAfter, "configure terminal", "bridge",
                f("lacp port admin-key %s %d", ifcName.replace("Ethernet", ""), adminkeyAug.getAdminKey()), "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        Config1 adminkeyAug = dataBefore.getAugmentation(Config1.class);

        if (adminkeyAug == null || adminkeyAug.getAdminKey() == null) {
            return;
        }

        blockingDeleteAndRead(cli, id, "configure terminal", "bridge",
                f("no lacp port admin-key %s", ifcName.replace("Ethernet", "")), "end");
    }
}
