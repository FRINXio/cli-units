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

package io.frinx.cli.unit.ios.qos.handler.classifier;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.IosQosClassifierAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.IosQosClassifierAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.classifier.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.classifier.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClassifierConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public ClassifierConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        configBuilder.setName(name);
        parseQosConfig(blockingRead(f(TermReader.SH_TERMS, name), cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseQosConfig(String output, ConfigBuilder configBuilder) {
        IosQosClassifierAugBuilder qosIosClassifierConfig = new IosQosClassifierAugBuilder();
        ParsingUtils.parseField(output, 0,
            ClassifierReader.CLASS_LINE::matcher,
            matcher -> matcher.group("type"),
            qosIosClassifierConfig::setStatementsMatching);
        configBuilder.addAugmentation(IosQosClassifierAug.class, qosIosClassifierConfig.build());
    }
}