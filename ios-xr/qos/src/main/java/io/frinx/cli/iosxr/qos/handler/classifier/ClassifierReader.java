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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.qos.handler.scheduler.SchedulerPolicyReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.ClassifierKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClassifierReader implements CliConfigListReader<Classifier, ClassifierKey, ClassifierBuilder> {

    static final String SH_CLASS_MAPS = "show running-config class-map | include ^class-map";
    private static final Pattern CLASSIFIER_NAME_LINE = Pattern.compile("class-map match-(any|all) (?<name>.+)");
    static final String DEFAULT_CLASS_SUFFIX = "-default";

    private Cli cli;

    public ClassifierReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ClassifierKey> getAllIds(@Nonnull InstanceIdentifier<Classifier> instanceIdentifier, @Nonnull
            ReadContext readContext) throws ReadFailedException {
        List<ClassifierKey> allKeys = new ArrayList<>();
        // read match class-maps
        String output = blockingRead(SH_CLASS_MAPS, cli, instanceIdentifier, readContext);
        allKeys.addAll(getClassifierMapKeys(output));
        // read class-default
        output = blockingRead(SchedulerPolicyReader.SH_POLICY_MAPS, cli, instanceIdentifier, readContext);
        allKeys.addAll(getClassifierDefaultKeys(output));
        return allKeys;
    }

    @VisibleForTesting
    static List<ClassifierKey> getClassifierMapKeys(String output) {
        return ParsingUtils.parseFields(output, 0, CLASSIFIER_NAME_LINE::matcher,
            matcher -> matcher.group("name"), ClassifierKey::new);
    }

    @VisibleForTesting
    static List<ClassifierKey> getClassifierDefaultKeys(String output) {
        return ParsingUtils.parseFields(output, 0, SchedulerPolicyReader.POLICY_NAME_LINE::matcher,
            matcher -> matcher.group("name"), n -> new ClassifierKey(n + DEFAULT_CLASS_SUFFIX));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Classifier> instanceIdentifier, @Nonnull
            ClassifierBuilder builder, @Nonnull ReadContext readContext) throws ReadFailedException {
        ClassifierKey key = instanceIdentifier.firstKeyOf(Classifier.class);
        builder.setName(key.getName());
    }
}
