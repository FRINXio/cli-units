/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.rpd;

import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.top.rpd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceRpdConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            configure interface {$name}
            {% if($config.rpd_index) %}rpd-index {$config.rpd_index}
            {% endif %}{% if($config.ucam) %}ucam {$config.ucam} dcam {$config.dcam}
            {% endif %}{% if($config.mac_address) %}mac-address {$config.mac_address}
            {% endif %}{% if($adp_enable) %}adp enable
            {% else %}no adp enable
            {% endif %}{% if($enable) %}no shutdown
            {% endif %}{% if($ssd_enable) %}ssd-enable
            {% endif %}end""";

    private static final String UPDATE_TEMPLATE = """
            configure interface {$name}
            {% if($rpd_index) %}rpd-index {$rpd_index}
            {% endif %}{% if ($ucam) || ($dcam) %}ucam {$config.ucam} dcam {$config.dcam}
            {% endif %}{% if($enable) %}{$enable}
            {% endif %}{% if($mac_address) %}{$mac_address}
            {% endif %}{% if($adp_enable) %}{$adp_enable}
            {% endif %}{% if($ssd_enable) %}{$ssd_enable}
            {% endif %}end""";

    private static final String DELETE_TEMPLATE = "configure interface no {$name}\n"
            + "end";

    private final Cli cli;

    public InterfaceRpdConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                    fT(WRITE_TEMPLATE,
                            "config", dataAfter,
                            "name", name,
                            "adp_enable", (dataAfter.isAdpEnable() != null && dataAfter.isAdpEnable())
                                    ? Chunk.TRUE : null,
                            "enable", (dataAfter.isEnable() != null && dataAfter.isEnable())
                                    ? Chunk.TRUE : null,
                            "ssd_enable", (dataAfter.isSsdEnable() != null && dataAfter.isSsdEnable())
                                    ? Chunk.TRUE : null));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore, @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE,
                        "before", dataBefore,
                        "config", dataAfter,
                        "name", name,
                        "mac_address", updateMacAddress(dataBefore, dataAfter),
                        "adp_enable", updateAdpEnable(dataBefore, dataAfter),
                        "enable", updateEnable(dataBefore, dataAfter),
                        "ssd_enable", updateSsdEnable(dataBefore, dataAfter),
                        "rpd_index", updateRpdIndex(dataBefore, dataAfter),
                        "ucam", updateUcam(dataBefore, dataAfter),
                        "dcam", updateDcam(dataBefore, dataAfter)));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataBefore,
                fT(DELETE_TEMPLATE,
                        "name", name));
    }

    private String updateEnable(Config dataBefore, Config dataAfter) {
        Boolean enableBefore = dataBefore.isEnable();
        Boolean enableAfter = dataAfter.isEnable();
        if (!Objects.equals(enableAfter, enableBefore)) {
            return enableAfter ? "no shutdown" : "shutdown";
        }
        return null;
    }

    private String updateMacAddress(Config dataBefore, Config dataAfter) {
        String macAddressBefore = dataBefore != null ? dataBefore.getMacAddress() : null;
        String macAddressAfter = dataAfter != null ? dataAfter.getMacAddress() : null;

        if (!Objects.equals(macAddressBefore, macAddressAfter)) {
            return macAddressAfter != null ? "mac-address " + macAddressAfter : "no mac-address " + macAddressBefore;
        }

        return null;
    }

    private String updateAdpEnable(Config dataBefore, Config dataAfter) {
        Boolean adpEnableBefore = dataBefore.isAdpEnable();
        Boolean adpEnableAfter = dataAfter.isAdpEnable();
        if (!Objects.equals(adpEnableAfter, adpEnableBefore)) {
            return adpEnableAfter ? "adp enable" : "no adp enable";
        }
        return null;
    }

    private String updateSsdEnable(Config dataBefore, Config dataAfter) {
        Boolean ssdEnableBefore = dataBefore.isSsdEnable();
        Boolean ssdEnableAfter = dataAfter.isSsdEnable();
        if (!Objects.equals(ssdEnableAfter, ssdEnableBefore)) {
            return ssdEnableAfter ? "ssd-enable" : "no ssd-enable";
        }
        return null;
    }

    private Integer updateRpdIndex(Config dataBefore, Config dataAfter) {
        Integer rpdIndexBefore = dataBefore.getRpdIndex();
        Integer rpdIndexAfter = dataAfter.getRpdIndex();

        if (!Objects.equals(rpdIndexBefore, rpdIndexAfter)) {
            return rpdIndexAfter;
        }

        return null;
    }

    private Integer updateUcam(Config dataBefore, Config dataAfter) {
        Integer ucamBefore = dataBefore.getUcam();
        Integer ucamAfter = dataAfter.getUcam();

        if (!Objects.equals(ucamBefore, ucamAfter)) {
            return ucamAfter;
        }

        return null;
    }

    private Integer updateDcam(Config dataBefore, Config dataAfter) {
        Integer dcamBefore = dataBefore.getDcam();
        Integer dcamAfter = dataAfter.getDcam();

        if (!Objects.equals(dcamBefore, dcamAfter)) {
            return dcamAfter;
        }

        return null;
    }
}