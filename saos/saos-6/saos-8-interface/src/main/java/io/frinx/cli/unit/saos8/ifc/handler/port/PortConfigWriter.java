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

package io.frinx.cli.unit.saos8.ifc.handler.port;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_TEMPLATE_SAOS =
            "{% if ($enabled) %}port {$enabled} port {$data.name}\n{% endif %}"
            + "{$data|update(description,port set port `$data.name` description \"`$data.description`\"\n,)}"
            + "{$data|update(mtu,port set port `$data.name` max-frame-size `$data.mtu`\n,)}"
            + "configuration save";

    private Cli cli;

    public PortConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                 @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (EthernetCsmacd.class.equals(data.getType())) {
            throw new WriteFailedException.CreateFailedException(iid, data,
                    new IllegalArgumentException("Physical interface cannot be created"));
        } else if (Ieee8023adLag.class.equals(data.getType())) {
            throw new WriteFailedException.CreateFailedException(iid, data,
                    new IllegalArgumentException("Creating LAG interface is not permitted"));
        }
        return false;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                "Changing interface type is not permitted. Before: %s, After: %s",
                dataBefore.getType(), dataAfter.getType());
        try {
            blockingWriteAndRead(cli, iid, dataAfter, updateTemplate(dataBefore, dataAfter));
            return true;
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(iid, dataBefore, dataAfter, e);
        }
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (EthernetCsmacd.class.equals(dataBefore.getType())) {
            throw new WriteFailedException.DeleteFailedException(iid,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        } else if (Ieee8023adLag.class.equals(dataBefore.getType())) {
            throw new WriteFailedException.DeleteFailedException(iid,
                    new IllegalArgumentException("Deleting LAG interface is not permitted"));
        }
        return false;
    }

    @VisibleForTesting
    String updateTemplate(Config before, Config after) {
        return fT(WRITE_TEMPLATE_SAOS, "data", after, "before", before,
                "enabled", updateEnabled(before, after));
    }

    private String updateEnabled(Config dataBefore, Config dataAfter) {
        Boolean enabledBefore = dataBefore.isEnabled();
        Boolean enabledAfter = dataAfter.isEnabled();
        if (!Objects.equals(enabledAfter, enabledBefore)) {
            if (enabledAfter != null) {
                return enabledAfter ? "enable" : "disable";
            }
        }
        return null;
    }
}