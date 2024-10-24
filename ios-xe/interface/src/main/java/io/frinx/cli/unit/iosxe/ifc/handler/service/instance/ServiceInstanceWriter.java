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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoServiceInstanceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Encapsulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ServiceInstanceWriter implements CliWriter<IfCiscoServiceInstanceAug> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_UPDATE_TEMPLATE = """
            {% loop in $config.service_instances.service_instance as $service_instance counter=$i %}service instance{% loop in $trunks as $trunk counter=$j %}{% if ($j == $i) && ($trunk == true) %} trunk{% endif %}{% onEmpty %}{% endloop %} {$service_instance.config.id} ethernet{% if ($service_instance.config.evc) %} {$service_instance.config.evc}{% endif %}
            {% if ($service_instance.encapsulation) %}encapsulation{% loop in $untagged as $un counter=$j,$i %}{% if ($j == $i) && ($un == true) %} untagged{% endif %}{% if ($j == $i) && ($un == true) && ($service_instance.encapsulation.dot1q) %} ,{% endif %}{% onEmpty %}{% endloop %}{% if ($service_instance.encapsulation.dot1q) %} dot1q {$service_instance.encapsulation.dot1q|join(, )}{% endif %}
            {% endif %}{% loop in $service_instance.l2protocols.service_instance_l2protocol.l2protocol as $l2 %}l2protocol {$l2.config.protocol_type} {$l2.config.protocol|join( )}
            {% onEmpty %}{% endloop %}{% if ($service_instance.bridge_domain) %}bridge-domain {$service_instance.bridge_domain.value}{% if ($service_instance.bridge_domain.group_number) %} split-horizon group {$service_instance.bridge_domain.group_number}{% endif %}
            {% endif %}{% if ($service_instance.rewrite) %}rewrite {$service_instance.rewrite.type} tag {$service_instance.rewrite.operation} 1 symmetric
            {% endif %}exit
            {% onEmpty %}{% endloop %}end""";

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$ifcName}\n"
            + WRITE_UPDATE_TEMPLATE;

    private static final String UPDATE_TEMPLATE = "configure terminal\n"
            + "interface {$ifcName}\n"
            + "{% loop in $delete as $instance_to_delete counter=$i %}"
            + "no service instance "
            + "{% loop in $trunksBefore as $trunk counter=$j %}"
            + "{% if ($j == $i) && ($trunk == true) %}trunk {% endif %}"
            + "{% onEmpty %}{% endloop %}"
            + "{$instance_to_delete.config.id}\n"
            + "{% onEmpty %}{% endloop %}"

            + "{% loop in $config.service_instances.service_instance as $instance_to_update counter=$i %}"
            + "no service instance "
            + "{% loop in $trunks as $trunk counter=$j %}"
            + "{% if ($j == $i) && ($trunk == true) %}trunk {% endif %}"
            + "{% onEmpty %}{% endloop %}"
            + "{$instance_to_update.config.id}\n"
            + "{% onEmpty %}{% endloop %}"

            + WRITE_UPDATE_TEMPLATE;

    @SuppressWarnings("checkstyle:linelength")
    private static final String DELETE_TEMPLATE = """
            configure terminal
            interface {$ifcName}
            {% loop in $config.service_instances.service_instance as $service_instance counter=$i %}no service instance {% loop in $trunks as $trunk counter=$j %}{% if ($j == $i) && ($trunk == true) %}trunk {% endif %}{% onEmpty %}{% endloop %}{$service_instance.config.id}
            {% endloop %}end""";

    private final Cli cli;

    public ServiceInstanceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<IfCiscoServiceInstanceAug> instanceIdentifier,
                                       @NotNull IfCiscoServiceInstanceAug ifCiscoServiceInstanceAug,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        var serviceInstances = ifCiscoServiceInstanceAug.getServiceInstances().getServiceInstance();
        var trunks = getTrunks(serviceInstances);
        var untagged = getUntagged(serviceInstances);

        blockingWriteAndRead(cli, instanceIdentifier, ifCiscoServiceInstanceAug,
                fT(WRITE_TEMPLATE,
                        "ifcName", ifcName,
                        "config", ifCiscoServiceInstanceAug,
                        "trunks", trunks,
                        "untagged", untagged));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<IfCiscoServiceInstanceAug> id,
                                        @NotNull IfCiscoServiceInstanceAug dataBefore,
                                        @NotNull IfCiscoServiceInstanceAug dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();

        var instancesBefore = dataBefore.getServiceInstances();
        var instancesAfter = dataAfter.getServiceInstances();
        var exclusiveToDelete = instancesBefore.getServiceInstance().stream()
                .filter(instance -> instancesAfter.getServiceInstance().stream()
                        .noneMatch(afterInstance -> afterInstance.getId().equals(instance.getId())))
                .collect(Collectors.toList());

        var trunksBefore = getTrunks(exclusiveToDelete);
        var trunks = getTrunks(instancesAfter.getServiceInstance());
        var untagged = getUntagged(instancesAfter.getServiceInstance());

        blockingWriteAndRead(cli, id, dataAfter,
                fT(UPDATE_TEMPLATE,
                        "ifcName", ifcName,
                                "dataBefore", instancesBefore,
                                "delete", exclusiveToDelete,
                                "trunksBefore", trunksBefore,
                                "config", dataAfter,
                                "trunks", trunks,
                                "untagged", untagged));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<IfCiscoServiceInstanceAug> instanceIdentifier,
                                        @NotNull IfCiscoServiceInstanceAug ifCiscoServiceInstanceAug,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        var serviceInstancesAfter = ifCiscoServiceInstanceAug.getServiceInstances().getServiceInstance();

        var trunks = getTrunks(serviceInstancesAfter);
        blockingWriteAndRead(cli, instanceIdentifier, ifCiscoServiceInstanceAug,
                fT(DELETE_TEMPLATE,
                        "ifcName", ifcName,
                        "trunks", trunks,
                        "config", ifCiscoServiceInstanceAug));
    }

    @NotNull
    private static List<String> getTrunks(List<ServiceInstance> serviceInstancesAfter) {
        return serviceInstancesAfter.stream()
                .map(instance -> {
                    Boolean isTrunk = instance.getConfig().isTrunk();
                    return isTrunk != null ? isTrunk.toString() : "unknown";
                })
                .collect(Collectors.toList());
    }

    @NotNull
    private static List<String> getUntagged(List<ServiceInstance> serviceInstances) {
        return serviceInstances.stream()
                .map(instance -> {
                    Encapsulation encapsulation = instance.getEncapsulation();
                    return encapsulation != null && encapsulation.isUntagged() != null
                            && encapsulation.isUntagged() ? "true" : "false";
                })
                .collect(Collectors.toList());
    }
}