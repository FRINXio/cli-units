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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoServiceInstanceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.Encapsulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceInstanceWriter implements CliWriter<IfCiscoServiceInstanceAug> {

    private static final String TEMPLATE = "{% if ($serviceInstance) %}"
            + "configure terminal\n"
            + "interface {$ifcName}\n"
            + "{$serviceInstance}"
            + "end"
            + "{% endif %}";

    private final Cli cli;

    public ServiceInstanceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<IfCiscoServiceInstanceAug> instanceIdentifier,
                                       @Nonnull IfCiscoServiceInstanceAug ifCiscoServiceInstanceAug,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        checkServiceInstances(ifCiscoServiceInstanceAug, ifcName);
        blockingWriteAndRead(cli, instanceIdentifier, ifCiscoServiceInstanceAug,
                fT(TEMPLATE,
                        "ifcName", ifcName,
                        "serviceInstance", getServiceInstanceCommands(null, ifCiscoServiceInstanceAug)));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<IfCiscoServiceInstanceAug> id,
                                        @Nonnull IfCiscoServiceInstanceAug dataBefore,
                                        @Nonnull IfCiscoServiceInstanceAug dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        checkServiceInstances(dataAfter, ifcName);
        blockingWriteAndRead(cli, id, dataAfter,
                fT(TEMPLATE,
                        "ifcName", ifcName,
                        "serviceInstance", getServiceInstanceCommands(dataBefore, dataAfter)));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<IfCiscoServiceInstanceAug> instanceIdentifier,
                                        @Nonnull IfCiscoServiceInstanceAug ifCiscoServiceInstanceAug,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, ifCiscoServiceInstanceAug,
                fT(TEMPLATE,
                        "ifcName", ifcName,
                        "serviceInstance", getServiceInstanceCommands(ifCiscoServiceInstanceAug, null)));
    }

    private void checkServiceInstances(final IfCiscoServiceInstanceAug aug, final String ifcName) {
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

    private String getServiceInstanceCommands(final IfCiscoServiceInstanceAug before,
                                              final IfCiscoServiceInstanceAug after) {
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

}