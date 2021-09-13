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
package io.frinx.cli.unit.huawei.qos.handler.classifier;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosClassifierConfig.Operation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosClassifierAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosClassifierAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.classifier.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.classifier.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClassifierConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_ACL_NAME = "display current-configuration | include traffic classifier %s";

    private Cli cli;

    public ClassifierConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        final String showCommand = String.format(SH_ACL_NAME, name);
        parQosConfig(blockingRead(showCommand, cli, instanceIdentifier, readContext),
                configBuilder, name);
        configBuilder.setName(name);
    }

    @VisibleForTesting
    static void parQosConfig(String output, ConfigBuilder configBuilder, String aclName) {
        VrpQosClassifierAugBuilder vrpQosClassifierAugBuilder = new VrpQosClassifierAugBuilder();
        final Optional<String> operator = ParsingUtils.parseField(output, 0,
            parseClassifier(aclName)::matcher,
            matcher -> matcher.group("operation"));
        operator.ifPresent(o -> vrpQosClassifierAugBuilder.setOperation(getClassifierOperation(o)));
        configBuilder.addAugmentation(VrpQosClassifierAug.class, vrpQosClassifierAugBuilder.build());
    }

    private static Pattern parseClassifier(final String name) {
        final String regex = String.format("traffic classifier %s operator (?<operation>\\S+)", name);
        return Pattern.compile(regex);
    }

    private static Operation getClassifierOperation(final String type) {
        for (final Operation classOperation: Operation.values()) {
            if (type.equalsIgnoreCase(classOperation.getName())) {
                return classOperation;
            }
        }
        return null;
    }

}
