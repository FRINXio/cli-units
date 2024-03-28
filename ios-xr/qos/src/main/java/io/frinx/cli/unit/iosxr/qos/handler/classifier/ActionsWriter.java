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

package io.frinx.cli.unit.iosxr.qos.handler.classifier;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosRemarkQosGroupAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ActionsWriter implements CliWriter<Actions> {

    private static final String WRITE_CURR_ATTR = "policy-map {$name}\n"
            + "{% if ($delete) %}no {% endif %}class {$className}\n"
            + "{% if (!$delete) %}"
            + "{% if ($mpls) %}"
            + "set mpls experimental topmost {$mpls}\n{% else %}"
            + "no set mpls experimental topmost\n{% endif %}"
            + "{% if ($aug.set_qos_group) %}"
            + "set qos-group {% loop in $aug.set_qos_group as $qos %}"
            + ClassifierWriter.LIST_QOS
            + "{% else %}"
            + "no set qos-group\n{% endif %}"
            + "{% if ($aug.set_precedences) %}"
            + "set precedence {% loop in $aug.set_precedences as $prec %}"
            + ClassifierWriter.LIST_PREC
            + "{% else %}"
            + "no set precedence\n{% endif %}"
            + "{% endif %}root";

    private static final String DEFAULT_CLASS = "class-default";

    private Cli cli;

    public ActionsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Actions> instanceIdentifier, @NotNull Actions
            actions, @NotNull WriteContext writeContext) throws WriteFailedException {
        String className = instanceIdentifier.firstKeyOf(Classifier.class)
                .getName();
        if (className.endsWith(ClassifierReader.DEFAULT_CLASS_SUFFIX)) {
            className = DEFAULT_CLASS;
        }
        final String policyName = actions.getConfig()
                .getTargetGroup();
        if (policyName != null) {
            QosRemarkQosGroupAug aug = actions.getRemark() != null ? actions.getRemark()
                    .getConfig()
                    .getAugmentation(QosRemarkQosGroupAug.class) : null;
            blockingWriteAndRead(cli, instanceIdentifier, actions, fT(WRITE_CURR_ATTR,
                    "name", policyName,
                    "className", className,
                    "mpls", actions.getRemark() != null ? actions.getRemark()
                            .getConfig()
                            .getSetMplsTc() : null,
                    "aug", aug));
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Actions> id, @NotNull Actions dataBefore,
                                        @NotNull Actions dataAfter, @NotNull WriteContext writeContext) throws
            WriteFailedException {
        // policy name was removed
        if (dataAfter.getConfig() == null) {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        } else if (dataBefore.getConfig() != null && !dataBefore.getConfig()
                .getTargetGroup()
                .equals(dataAfter.getConfig()
                        .getTargetGroup())) {
            // policy reference changed or was added
            deleteCurrentAttributes(id, dataBefore, writeContext);
            writeCurrentAttributes(id, dataAfter, writeContext);
        } else {
            // only attributes changed, we're safe to 'write'
            writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Actions> instanceIdentifier, @NotNull Actions
            actions, @NotNull WriteContext writeContext) throws WriteFailedException {
        final String className = instanceIdentifier.firstKeyOf(Classifier.class)
                .getName();
        final String policyName = actions.getConfig()
                .getTargetGroup();
        if (policyName != null) {
            blockingWriteAndRead(cli, instanceIdentifier, actions, fT(WRITE_CURR_ATTR,
                    "name", policyName, "className", className, "delete", true));
        }
    }
}