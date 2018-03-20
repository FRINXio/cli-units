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

package io.frinx.cli.iosxr.qos.handler.classifier;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosRemarkQosGroupAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.common.remark.actions.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RemarkConfigWriter implements CliWriter<Config> {

    private Cli cli;

    private static final String WRITE_CURR_ATTR = "policy-map {$name}\n" +
        "class {$className}\n" +
        "{% if ($mpls) %}" +
        "set mpls experimental topmost {$mpls}\n{% else %}" +
        "no set mpls experimental topmost\n{% endif %}" +
        "{% if ($aug.set_qos_group) %}" +
        "set qos-group {$aug.set_qos_group}\n{% else %}" +
        "no set qos-group\n{% endif %}" +
        "{% if ($aug.set_precedences) %}" +
        "set precedence {% loop in $aug.set_precedences as $prec %}" + ClassifierWriter.LIST_PREC + "{% else %}" +
        "no set precedence\n{% endif %}" +
        "root";

    public RemarkConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String className = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        final String policyName = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Actions.class)).get().getConfig().getTargetGroup();
        if (policyName != null) {
            QosRemarkQosGroupAug aug = config.getAugmentation(QosRemarkQosGroupAug.class);
            blockingWriteAndRead(cli, instanceIdentifier, config, fT(WRITE_CURR_ATTR,
                "name", policyName,
                "className", className,
                "mpls", config.getSetMplsTc(),
                "aug", aug));
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore, @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String className = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        final String policyName = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Actions.class)).get().getConfig().getTargetGroup();
        if (policyName != null) {
            blockingWriteAndRead(cli, instanceIdentifier, config, fT(WRITE_CURR_ATTR,
                "name", policyName,
                "className", className));
        }
    }
}
