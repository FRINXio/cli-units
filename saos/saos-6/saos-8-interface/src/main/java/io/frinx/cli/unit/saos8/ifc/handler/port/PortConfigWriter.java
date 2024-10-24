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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_TEMPLATE_SAOS =
            """
                    {% if ($enabled) %}port {$enabled} port {$data.name}
                    {% endif %}{$data|update(description,port set port `$data.name` description "`$data.description`"
                    ,)}{$data|update(mtu,port set port `$data.name` max-frame-size `$data.mtu`
                    ,)}{% if ($speedType) %}port set port {$data.name} speed {$speedType}
                    {% endif %}{% if ($negotiationAuto) %}port set port {$data.name} auto-neg {$negotiationAuto}
                    {% endif %}""";

    private Cli cli;

    public PortConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid,
                                                 @NotNull Config data,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
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
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid,
                                                  @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
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
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid,
                                                  @NotNull Config dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
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
                "enabled", updateEnabled(before, after), "negotiationAuto", updateNegotiationAuto(before, after),
                "speedType", updateSpeedType(before, after));
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

    private String updateNegotiationAuto(Config dataBefore, Config dataAfter) {
        Boolean negotiationAutoBefore = setUpdateNegotiationAuto(dataBefore);
        Boolean negotiationAutoAfter = setUpdateNegotiationAuto(dataAfter);
        if (!Objects.equals(negotiationAutoAfter, negotiationAutoBefore)) {
            if (negotiationAutoAfter != null) {
                return negotiationAutoAfter ? "on" : "off";
            }
        }
        return null;
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    private Boolean setUpdateNegotiationAuto(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            return ifSaosAug.isNegotiationAuto();
        }
        return null;
    }

    private String updateSpeedType(Config dataBefore, Config dataAfter) {
        SaosIfExtensionConfig.SpeedType speedTypeBefore = setUpdateSpeedType(dataBefore);
        SaosIfExtensionConfig.SpeedType speedTypeAfter = setUpdateSpeedType(dataAfter);
        if (!Objects.equals(speedTypeAfter, speedTypeBefore)) {
            if (speedTypeAfter != null) {
                return speedTypeAfter.getName();
            }
        }
        return null;
    }

    private SaosIfExtensionConfig.SpeedType setUpdateSpeedType(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            return ifSaosAug.getSpeedType();
        }
        return null;
    }
}