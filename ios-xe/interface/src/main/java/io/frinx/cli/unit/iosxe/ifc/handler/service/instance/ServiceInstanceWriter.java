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
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoServiceInstanceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.L2protocolConfig.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.L2protocolConfig.ProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.L2protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.BridgeDomain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Encapsulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.L2protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Rewrite;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ServiceInstanceWriter implements CliWriter<IfCiscoServiceInstanceAug> {

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
        blockingWriteAndRead(cli, instanceIdentifier, ifCiscoServiceInstanceAug,
                fT(TEMPLATE,
                        "ifcName", ifcName,
                        "serviceInstance", getCommands(null, ifCiscoServiceInstanceAug)));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<IfCiscoServiceInstanceAug> id,
                                        @Nonnull IfCiscoServiceInstanceAug dataBefore,
                                        @Nonnull IfCiscoServiceInstanceAug dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, id, dataAfter,
                fT(TEMPLATE,
                        "ifcName", ifcName,
                        "serviceInstance", getCommands(dataBefore, dataAfter)));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<IfCiscoServiceInstanceAug> instanceIdentifier,
                                        @Nonnull IfCiscoServiceInstanceAug ifCiscoServiceInstanceAug,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, ifCiscoServiceInstanceAug,
                fT(TEMPLATE,
                        "ifcName", ifcName,
                        "serviceInstance", getCommands(ifCiscoServiceInstanceAug, null)));
    }

    private String getCommands(final IfCiscoServiceInstanceAug before,
                               final IfCiscoServiceInstanceAug after) {
        final StringBuilder currentInstances = new StringBuilder();

        if (before != null) {
            final ServiceInstances serviceInstances = before.getServiceInstances();
            if (serviceInstances != null) {
                final List<ServiceInstance> serviceInstanceList = serviceInstances.getServiceInstance();
                if (serviceInstanceList != null) {
                    for (final ServiceInstance serviceInstance : serviceInstanceList) {
                        currentInstances.append(getCreationCommands(serviceInstance, true));
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
                        currentInstances.append(getCreationCommands(serviceInstance, false));
                        currentInstances.append(getEncapsulationCommands(serviceInstance.getEncapsulation()));
                        currentInstances.append(getL2ProtocolCommands(serviceInstance.getL2protocols()));
                        currentInstances.append(getBridgeDomainCommands(serviceInstance.getBridgeDomain()));
                        currentInstances.append(getRewrite(serviceInstance));
                        currentInstances.append("exit\n");
                    }
                }
            }
        }

        return currentInstances.toString();
    }

    private String getRewrite(ServiceInstance serviceInstance) {
        Rewrite rewrite = serviceInstance.getRewrite();
        if (rewrite != null && rewrite.getType() != null && rewrite.getOperation() != null) {
            if (rewrite.getType().getName().equals("ingress") && rewrite.getOperation().getName().equals("pop")) {
                return "rewrite ingress tag pop 1 symmetric\n";
            } else {
                throw new IllegalArgumentException(
                        String.format("Rewrite contains unsupported type(%s) or protocol(%s)!",
                                rewrite.getType().getName(),
                                rewrite.getOperation().getName()));
            }
        }
        return "";
    }

    private String getCreationCommands(final ServiceInstance serviceInstance, boolean delete) {
        final StringBuilder creationCommands = new StringBuilder();
        final Config config = serviceInstance.getConfig();

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

    private String getBridgeDomainCommands(final BridgeDomain bridgeDomain) {
        final StringBuilder bridgeDomainCommand = new StringBuilder();

        if (bridgeDomain != null) {
            if (bridgeDomain.getValue() != null) {
                bridgeDomainCommand.append("bridge-domain ").append(bridgeDomain.getValue());
                if (bridgeDomain.getGroupNumber() != null) {
                    bridgeDomainCommand.append(" split-horizon group ").append(bridgeDomain.getGroupNumber());
                }
                bridgeDomainCommand.append("\n");
            }
        }

        return bridgeDomainCommand.toString();
    }

    private String getEncapsulationCommands(final Encapsulation encapsulation) {
        final StringBuilder configCommands = new StringBuilder();

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

        return configCommands.toString();
    }

    private String getL2ProtocolCommands(final L2protocols l2protocols) {
        final StringBuilder creationCommands = new StringBuilder();
        if (l2protocols != null && l2protocols.getServiceInstanceL2protocol() != null) {
            for (L2protocol l2protocol: l2protocols.getServiceInstanceL2protocol().getL2protocol()) {
                final ProtocolType protocolType = l2protocol.getConfig().getProtocolType();
                final List<Protocol> protocol = l2protocol.getConfig().getProtocol();

                creationCommands.append("l2protocol ");

                if (protocolType != null) {
                    creationCommands.append(protocolType.getName()).append(" ");
                }
                if (protocol != null) {
                    creationCommands.append(protocol.stream()
                            .map(Protocol::getName)
                            .collect(Collectors.joining(" ")));
                }
                creationCommands.append("\n");
            }
        }

        return creationCommands.toString();
    }
}
