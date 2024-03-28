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
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherLacpAdminkeyConfigWriter implements CliWriter<Config>, CompositeWriter.Child<Config> {

    private final Cli cli;

    public BundleEtherLacpAdminkeyConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {
        return writeOrUpdateBundleEtherLacpAdminkey(id, dataAfter, false);
    }

    @Override
    public boolean updateCurrentAttributesWResult(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataBefore,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        return writeOrUpdateBundleEtherLacpAdminkey(id, dataAfter, true);
    }

    private boolean writeOrUpdateBundleEtherLacpAdminkey(
            InstanceIdentifier<Config> id,
            Config dataAfter,
            boolean isPresent) throws WriteFailedException {

        String portId = id.firstKeyOf(Interface.class).getName().replace("Ethernet", "");
        Integer adminKey = getAdminKey(dataAfter);

        if (!isPresent && adminKey == null) {
            return false;
        }

        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                "bridge",
                adminKey == null
                    ? f("no lacp port admin-key %s", portId)
                    : f("lacp port admin-key %s %d", portId, adminKey),
                "end");
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String portId = id.firstKeyOf(Interface.class).getName().replace("Ethernet", "");
        Config1 adminkeyAug = dataBefore.getAugmentation(Config1.class);

        if (adminkeyAug == null || adminkeyAug.getAdminKey() == null) {
            return false;
        }
        blockingDeleteAndRead(cli, id,
                "configure terminal",
                "bridge",
                f("no lacp port admin-key %s", portId),
                "end");
        return true;
    }

    private static Integer getAdminKey(Config config) {
        Config1 adminkeyAug = config.getAugmentation(Config1.class);
        if (adminkeyAug == null) {
            return null;
        }

        return adminkeyAug.getAdminKey();
    }
}