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

package io.frinx.cli.unit.saos.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


@SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
public class InterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE_SAOS =
            // enable/disable port
            "{% if ($enabled) %}port {$enabled} port {$data.name}\n{% endif %}"
            // description
            + "{$data|update(description,port set port `$data.name` description \"`$data.description`\"\n,"
            + "port unset port `$data.name` description\n)}"
            // max-frame-size
            + "{$data|update(mtu,port set port `$data.name` max-frame-size `$data.mtu`\n,)}"
            // acceptable-frame-type
            + "{% if ($aft) %}port set port {$data.name} acceptable-frame-type {$aft}\n{% endif %}"
            // mode
            + "{% if ($pt) %}port set port {$data.name} mode {$pt}\n{% endif %}"
            // vs-ingress-filter
            + "{% if ($vif) %}port set port {$data.name} vs-ingress-filter {$vif}\n{% endif %}"
            // resolved-cos-remark-l2
            + "{% if ($rcrl) %}port set port {$data.name} resolved-cos-remark-l2 {$rcrl}\n{% endif %}"
            // vlan-ethertype-policy
            + "{% if ($vep) %}virtual-circuit ethernet set port {$data.name} vlan-ethertype-policy {$vep}\n{% endif %}"
            // ingress-to-egress-qmap
            + "{% if ($iteq) %}port set port {$data.name} ingress-to-egress-qmap {$iteq}\n{% endif %}"
            // flow access-control
            + "{% if ($max_macs) %}flow access-control set port {$data.name} max-dynamic-macs {$max_macs}\n{% endif %}"
            + "{% if ($fwd_un) %}flow access-control set port {$data.name} forward-unlearned {$fwd_un}\n{% endif %}"
            // rstp disable port
            + "{% if ($rstp_enabled) %}rstp {$rstp_enabled} port {$data.name}\n{% endif %}"
            // mstp disable port
            + "{% if ($mstp_enabled) %}mstp {$mstp_enabled} port {$data.name}\n{% endif %}"
            // speed
            + "{% if ($speedType) %}port set port {$data.name} speed {$speedType}\n{% endif %}"
            // negotiation auto
            + "{% if ($negotiationAuto) %}port set port {$data.name} auto-neg {$negotiationAuto}\n{% endif %}";

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config data,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        if (EthernetCsmacd.class.equals(data.getType())) {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Physical interface cannot be created"));
        } else if (Ieee8023adLag.class.equals(data.getType())) {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Creating LAG interface is not permitted"));
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                "Changing interface type is not permitted. Before: %s, After: %s",
                dataBefore.getType(), dataAfter.getType());
        try {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (EthernetCsmacd.class.equals(dataBefore.getType())) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        } else if (Ieee8023adLag.class.equals(dataBefore.getType())) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Deleting LAG interface is not permitted"));
        }
    }

    @VisibleForTesting
    String updateTemplate(Config dateBefore, Config dataAfter) {
        return fT(WRITE_TEMPLATE_SAOS, "before", dateBefore,
                "data", dataAfter,
                "enabled", updateEnabled(dateBefore, dataAfter),
                "pt", updatePhysicalType(dateBefore, dataAfter),
                "aft", updateAcceptableFrameType(dateBefore, dataAfter),
                "vif", updateIngressFilter(dateBefore, dataAfter),
                "vep", updateVlanEthertypePolicy(dateBefore, dataAfter),
                "iteq", updateIngressQmap(dateBefore, dataAfter),
                "max_macs", updateMaxDynamicMacs(dateBefore, dataAfter),
                "fwd_un", updateForwardUnlearned(dateBefore, dataAfter),
                "rstp_enabled", updateRstpEnabled(dateBefore, dataAfter),
                "mstp_enabled", updateMstpEnabled(dateBefore, dataAfter),
                "rcrl", updateResolvedCosRemarkL2(dateBefore, dataAfter),
                "negotiationAuto", updateNegotiationAuto(dateBefore, dataAfter),
                "speedType", updateSpeedType(dateBefore, dataAfter));
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

    private String updatePhysicalType(Config dataBefore, Config dataAfter) {
        String typeBefore = setPhysicalType(dataBefore);
        String typeAfter = setPhysicalType(dataAfter);
        if (!Objects.equals(typeAfter, typeBefore)) {
            if (typeAfter != null) {
                return typeAfter;
            }
        }
        return null;
    }

    private String setPhysicalType(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            SaosIfExtensionConfig.PhysicalType physicalType = ifSaosAug.getPhysicalType();
            return physicalType != null ? physicalType.getName() : null;
        }
        return null;
    }

    private String updateAcceptableFrameType(Config dataBefore, Config dataAfter) {
        String typeBefore = setAcceptableFrameType(dataBefore);
        String typeAfter = setAcceptableFrameType(dataAfter);
        if (!Objects.equals(typeAfter, typeBefore)) {
            if (typeAfter != null) {
                return typeAfter;
            }
        }
        return null;
    }

    private String setAcceptableFrameType(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            SaosIfExtensionConfig.AcceptableFrameType acceptableFrameType = ifSaosAug.getAcceptableFrameType();
            return acceptableFrameType != null ? acceptableFrameType.getName() : null;
        }
        return null;
    }

    private String updateIngressFilter(Config dataBefore, Config dataAfter) {
        Boolean ingressBefore = setIngressFilter(dataBefore);
        Boolean ingressAfter = setIngressFilter(dataAfter);
        if (!Objects.equals(ingressAfter, ingressBefore)) {
            if (ingressAfter != null) {
                return ingressAfter ? "on" : "off";
            }
        }
        return null;
    }

    private Boolean setIngressFilter(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            return ifSaosAug.isVsIngressFilter();
        }
        return null;
    }

    private String updateVlanEthertypePolicy(Config dataBefore, Config dataAfter) {
        String policyBefore = setVlanEthertypePolicy(dataBefore);
        String policyAfter = setVlanEthertypePolicy(dataAfter);
        if (!Objects.equals(policyAfter, policyBefore)) {
            if (policyAfter != null) {
                return policyAfter;
            }
        }
        return null;
    }

    private String setVlanEthertypePolicy(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            SaosIfExtensionConfig.VlanEthertypePolicy vlanEthertypePolicy = ifSaosAug.getVlanEthertypePolicy();
            return vlanEthertypePolicy != null ? vlanEthertypePolicy.getName() : null;
        }
        return null;
    }

    private String updateIngressQmap(Config dataBefore, Config dataAfter) {
        String qmapBefore = setIngressQmap(dataBefore);
        String qmapAfter = setIngressQmap(dataAfter);
        if (!Objects.equals(qmapAfter, qmapBefore)) {
            if (qmapAfter != null) {
                return qmapAfter;
            }
        }
        return null;
    }

    private String setIngressQmap(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            SaosIfExtensionConfig.IngressToEgressQmap qmap = ifSaosAug.getIngressToEgressQmap();
            return qmap != null ? qmap.getName() : null;
        }
        return null;
    }

    private Integer updateMaxDynamicMacs(Config dataBefore, Config dataAfter) {
        Integer dynamicMacsBefore = setMaxDynamicMacs(dataBefore);
        Integer dynamicMacsAfter = setMaxDynamicMacs(dataAfter);
        if (!Objects.equals(dynamicMacsAfter, dynamicMacsBefore)) {
            if (dynamicMacsAfter != null) {
                return dynamicMacsAfter;
            }
        }
        return null;
    }

    private Integer setMaxDynamicMacs(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            Integer maxDynamicMacs = ifSaosAug.getMaxDynamicMacs();
            return maxDynamicMacs != null ? maxDynamicMacs : null;
        }
        return null;
    }

    private String updateForwardUnlearned(Config dataBefore, Config dataAfter) {
        Boolean forwardBefore = setForwardUnlearned(dataBefore);
        Boolean forwardAfter = setForwardUnlearned(dataAfter);
        if (!Objects.equals(forwardAfter, forwardBefore)) {
            if (forwardAfter != null) {
                return forwardAfter ? "on" : "off";
            }
        }
        return null;
    }

    private Boolean setForwardUnlearned(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            return ifSaosAug.isForwardUnlearned();
        }
        return null;
    }

    private String updateRstpEnabled(Config dataBefore, Config dataAfter) {
        Boolean enabledBefore = setRstpEnabled(dataBefore);
        Boolean enabledAfter = setRstpEnabled(dataAfter);
        if (!Objects.equals(enabledAfter, enabledBefore)) {
            if (enabledAfter != null) {
                return enabledAfter ? "enable" : "disable";
            }
        }
        return null;
    }

    private Boolean setRstpEnabled(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            return ifSaosAug.isRstpEnabled();
        }
        return null;
    }

    private String updateMstpEnabled(Config dataBefore, Config dataAfter) {
        Boolean enabledBefore = setMstpEnabled(dataBefore);
        Boolean enabledAfter = setMstpEnabled(dataAfter);
        if (!Objects.equals(enabledAfter, enabledBefore)) {
            if (enabledAfter != null) {
                return enabledAfter ? "enable" : "disable";
            }
        }
        return null;
    }

    private Boolean setMstpEnabled(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            return ifSaosAug.isMstpEnabled();
        }
        return null;
    }

    private String updateResolvedCosRemarkL2(Config dataBefore, Config dataAfter) {
        Boolean rcrlEnabledBefore = setResolvedCosRemarkL2(dataBefore);
        Boolean rcrlEnabledAfter = setResolvedCosRemarkL2(dataAfter);
        if (!Objects.equals(rcrlEnabledAfter, rcrlEnabledBefore)) {
            if (rcrlEnabledAfter != null) {
                return rcrlEnabledAfter ? "true" : "false";
            }
        }
        return null;
    }

    private Boolean setResolvedCosRemarkL2(Config config) {
        IfSaosAug ifSaosAug = config.getAugmentation(IfSaosAug.class);
        if (ifSaosAug != null) {
            return ifSaosAug.isResolvedCosRemarkL2();
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