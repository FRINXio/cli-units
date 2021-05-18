/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler;

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.Util;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.Encapsulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{% if ($data.mtu) %}mtu {$data.mtu}\n{% else %}no mtu\n{% endif %}"
            + "{% if ($data.description) %}description {$data.description}\n{% else %}no description\n{% endif %}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "{% if ($pt) %}{$pt}{% endif %}"
            + "{% if ($stormControl) %}{$stormControl}{% endif %}"
            + "{% if ($lldpTransmit) %}{$lldpTransmit}{% endif %}"
            + "{% if ($lldpReceive) %}{$lldpReceive}{% endif %}"
            + "{% if ($serviceInstance) %}{$serviceInstance}{% endif %}"
            + "end";

    private static final String WRITE_TEMPLATE_VLAN = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{% if ($data.mtu) %}mtu {$data.mtu}\n{% else %}no mtu\n{% endif %}"
            + "{% if ($data.description) %}description {$data.description}\n{% else %}no description\n{% endif %}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no interface {$data.name}\n"
            + "end";

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        final IfCiscoExtAug ciscoExtAug = getIfCiscoExtAug(after);
        final IfCiscoExtAug ciscoExtAugBefore = getIfCiscoExtAug(before);
        if (isPhysicalInterface(after)) {
            checkServiceInstances(ciscoExtAug, after.getName());
            return fT(WRITE_TEMPLATE,
                    "before", before,
                    "data", after,
                    "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
                    "pt", getPhysicalType(before, after),
                    "stormControl", getStormControlCommands(ciscoExtAugBefore, ciscoExtAug),
                    "lldpTransmit", getLldpTransmit(ciscoExtAugBefore, ciscoExtAug),
                    "lldpReceive", getLldpReceive(ciscoExtAugBefore, ciscoExtAug),
                    "serviceInstance", getServiceInstanceCommands(ciscoExtAugBefore, ciscoExtAug));
        }
        return fT(WRITE_TEMPLATE_VLAN,
                "before", before,
                "data", after,
                "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
    }

    private IfCiscoExtAug getIfCiscoExtAug(final Config config) {
        if (config != null) {
            return config.getAugmentation(IfCiscoExtAug.class);
        }
        return null;
    }

    private String getCommandHelper(final Boolean before, final Boolean after, final String command) {
        if (!before && after) {
            return command;
        } else if (before && !after) {
            return "no " + command;
        }
        return null;
    }

    private String getLldpTransmit(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Boolean beforeLldpTransmit = true;
        Boolean afterLldpTransmit = true;
        if (before != null && before.isLldpTransmit() != null) {
            beforeLldpTransmit = before.isLldpTransmit();
        }
        if (after != null && after.isLldpTransmit() != null) {
            afterLldpTransmit = after.isLldpTransmit();
        }
        return getCommandHelper(beforeLldpTransmit, afterLldpTransmit, "lldp transmit\n");
    }

    private String getLldpReceive(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        Boolean beforeLldpReceive = true;
        Boolean afterLldpReceive = true;
        if (before != null && before.isLldpReceive() != null) {
            beforeLldpReceive = before.isLldpReceive();
        }
        if (after != null && after.isLldpReceive() != null) {
            afterLldpReceive = after.isLldpReceive();
        }
        return getCommandHelper(beforeLldpReceive, afterLldpReceive, "lldp receive\n");
    }

    private String getPhysicalType(final Config dataBefore, final Config dataAfter) {
        final String typeBefore = getPhysicalType(dataBefore);
        final String typeAfter = getPhysicalType(dataAfter);
        if (!Objects.equals(typeAfter, typeBefore)) {
            if (typeAfter != null) {
                return "media-type " + typeAfter + "\n";
            }
            return "no media-type\n";
        }
        return null;
    }

    private String getPhysicalType(final Config config) {
        if (config != null) {
            final IfSaosAug saosAug = config.getAugmentation(IfSaosAug.class);
            if (saosAug != null) {
                final PhysicalType physicalType = saosAug.getPhysicalType();
                return physicalType != null ? physicalType.getName() : null;
            }
        }
        return null;
    }

    private String getStormControlCommands(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        final StringBuilder currentControls = new StringBuilder();

        if (after != null) {
            if (after.getStormControl() != null) {
                // create commands for required storm controls
                for (final StormControl stormControl : after.getStormControl()) {
                    currentControls.append("storm-control ").append(stormControl.getAddress().getName());
                    currentControls.append(" level ").append(stormControl.getLevel().toString()).append("\n");
                }
            }
        }

        if (before != null) {
            // create no-commands for remaining storm controls only if they were there before
            if (before.getStormControl() != null) {
                for (final StormControl.Address address : StormControl.Address.values()) {
                    for (final StormControl stormControl : before.getStormControl()) {
                        if (!currentControls.toString().contains(address.getName())
                                && stormControl.getAddress().getName().equals(address.getName())) {
                            currentControls.append("no storm-control ").append(address.getName()).append(" level\n");
                        }
                    }
                }
            }
        }

        return currentControls.toString();
    }

    private void checkServiceInstances(final IfCiscoExtAug aug, final String ifcName) {
        if (aug != null && aug.getServiceInstances() != null) {
            final List<ServiceInstance> serviceInstanceList = aug.getServiceInstances().getServiceInstance();
            if (serviceInstanceList != null) {
                int trunkCount = 0;
                int untaggedVlanCount = 0;
                final Set<Integer> vlanIds = new HashSet<>();

                for (final ServiceInstance serviceInstance : serviceInstanceList) {
                    final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024
                            .service.instance.top.service.instances.service.instance.Config config =
                            serviceInstance.getConfig();

                    if (config != null) {
                        if (config.isTrunk() != null && config.isTrunk()) {
                            trunkCount++;
                        }
                        checkServiceInstanceTrunk(trunkCount, serviceInstance, ifcName);

                        final Encapsulation encapsulation = config.getEncapsulation();
                        if (encapsulation != null) {
                            if (encapsulation.isUntagged() != null && encapsulation.isUntagged()) {
                                untaggedVlanCount++;
                            }
                            checkServiceInstanceEncapsulation(untaggedVlanCount, vlanIds, serviceInstance, ifcName);
                        }
                    }
                }
            }
        }
    }

    private void checkServiceInstanceTrunk(int trunkCount,
                                           final ServiceInstance serviceInstance,
                                           final String ifcName) {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance
                .top.service.instances.service.instance.Config config = serviceInstance.getConfig();

        if (trunkCount > 1) {
            throw new IllegalStateException(
                    f("%s: Only one trunk service instance is allowed per interface", ifcName));
        }

        final boolean isTrunk = config.isTrunk() != null && config.isTrunk();
        final boolean hasEvc = config.getEvc() != null && config.getEvc().length() > 0;
        if (isTrunk && hasEvc) {
            throw new IllegalStateException(
                    f("%s: Attaching EVC to trunk service instance (id: %s) is not supported",
                            ifcName, serviceInstance.getId()));
        }
    }

    private void checkServiceInstanceEncapsulation(final int untaggedVlanCount,
                                                   final Set<Integer> vlanIds,
                                                   final ServiceInstance serviceInstance,
                                                   final String ifcName) {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance
                .top.service.instances.service.instance.Config config = serviceInstance.getConfig();

        final Encapsulation encapsulation = config.getEncapsulation();
        final boolean isTrunk = config.isTrunk() != null && config.isTrunk();
        final boolean isUntagged = encapsulation.isUntagged() != null && encapsulation.isUntagged();

        if (isTrunk && isUntagged) {
            throw new IllegalStateException(
                    f("%s: Untagged encapsulation in trunk service instance (id: %s) "
                            + "is not supported", ifcName, serviceInstance.getId()));
        }
        if (untaggedVlanCount > 1) {
            throw new IllegalStateException(
                    f("%s: Untagged encapsulation is already configured under other service "
                            + "instance", ifcName));
        }
        if (encapsulation.getDot1q() != null) {
            if (vlanIds.stream().anyMatch(encapsulation.getDot1q()::contains)) {
                throw new IllegalStateException(
                        f("%s: Some vlan ids are already configured under other service "
                                + "instance ", ifcName));
            } else {
                vlanIds.addAll(encapsulation.getDot1q());
            }
        }
    }

    private String getServiceInstanceCommands(final IfCiscoExtAug before, final IfCiscoExtAug after) {
        final StringBuilder currentInstances = new StringBuilder();

        if (before != null) {
            final ServiceInstances serviceInstances = before.getServiceInstances();
            if (serviceInstances != null) {
                final List<ServiceInstance> serviceInstanceList = serviceInstances.getServiceInstance();
                if (serviceInstanceList != null) {
                    for (final ServiceInstance serviceInstance : serviceInstanceList) {
                        currentInstances.append(getServiceInstanceCreationCommands(serviceInstance, true));
                    }
                }
            }
        }

        if (after != null) {
            final ServiceInstances serviceInstances = after.getServiceInstances();
            if (serviceInstances != null) {
                final List<ServiceInstance> serviceInstanceList = serviceInstances.getServiceInstance();
                if (serviceInstanceList != null) {
                    for (final ServiceInstance serviceInstance : serviceInstanceList) {
                        currentInstances.append(getServiceInstanceCreationCommands(serviceInstance, false));
                        currentInstances.append(getServiceInstanceConfigCommands(serviceInstance.getConfig()));
                    }
                }
            }
        }

        return currentInstances.toString();
    }

    private String getServiceInstanceCreationCommands(final ServiceInstance serviceInstance, boolean delete) {
        final StringBuilder creationCommands = new StringBuilder();
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance
                .top.service.instances.service.instance.Config config = serviceInstance.getConfig();

        final boolean isTrunk = config != null && config.isTrunk() != null && config.isTrunk();
        final boolean hasEvc = config != null && config.getEvc() != null && config.getEvc().length() > 0;

        if (delete) {
            creationCommands.append("no ");
        }
        creationCommands.append("service instance ");
        if (isTrunk) {
            creationCommands.append("trunk ");
        }
        creationCommands.append(serviceInstance.getId());
        if (!delete) {
            creationCommands.append(" ethernet");
        }
        if (hasEvc && !delete) {
            creationCommands.append(" ").append(serviceInstance.getConfig().getEvc());
        }
        creationCommands.append("\n");

        return creationCommands.toString();
    }

    private String getServiceInstanceConfigCommands(final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                                    .service.instance.Config config) {
        final StringBuilder configCommands = new StringBuilder();

        if (config != null) {
            final Encapsulation encapsulation = config.getEncapsulation();
            if (encapsulation != null) {
                final boolean isUntagged = encapsulation.isUntagged() != null && encapsulation.isUntagged();
                final boolean hasDot1q = encapsulation.getDot1q() != null && encapsulation.getDot1q().size() > 0;

                if (isUntagged || hasDot1q) {
                    configCommands.append("encapsulation ");
                }
                if (isUntagged) {
                    configCommands.append("untagged");
                }
                if (isUntagged && hasDot1q) {
                    configCommands.append(" , ");
                }
                if (hasDot1q) {
                    configCommands.append("dot1q ");
                    configCommands.append(encapsulation
                            .getDot1q()
                            .stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(" , ")));
                }
                if (isUntagged || hasDot1q) {
                    configCommands.append("\n");
                }
            }
        }

        configCommands.append("exit\n");
        return configCommands.toString();
    }

    @Override
    protected boolean isPhysicalInterface(Config data) {
        return Util.isPhysicalInterface(data.getType());
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }

}