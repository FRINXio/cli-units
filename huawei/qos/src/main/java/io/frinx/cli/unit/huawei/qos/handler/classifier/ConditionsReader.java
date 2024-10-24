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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class ConditionsReader implements CliConfigReader<Conditions, ConditionsBuilder> {

    private Cli cli;

    public ConditionsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Conditions> instanceIdentifier,
                                      @NotNull ConditionsBuilder conditionsBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        if (name.endsWith(ClassifierReader.DEFAULT_CLASS_SUFFIX)) {
            // we are reading class-default that does not have any matches, skip it
            return;
        }

        final String line = instanceIdentifier.firstKeyOf(Term.class).getId();
        final String output = blockingRead(f(TermReader.SH_TERMS, name), cli, instanceIdentifier, readContext);
        filterParsing(output, line, conditionsBuilder);
    }

    @VisibleForTesting
    public static void filterParsing(String output, String line, ConditionsBuilder conditionsBuilder) {
        final String type = parseClassMapType(line);
        // if we have match-all type, all conditions will be parsed into one term
        if (ClassMapType.MATCH_ALL.getStringValue().equals(type)) {
            parseConditions(output, "", conditionsBuilder);
        } else {
            // if we have a number as term id, just the condition on the specific line will be parsed
            parseConditions(output, line, conditionsBuilder);
        }
    }

    private static void parseConditions(String output, String line, ConditionsBuilder conditionsBuilder) {
        // although we might have only one line of output, we don't know what type of match we have, try all
        try {
            Ipv4Util.parseIpv4(output, line, conditionsBuilder);
        } catch (IllegalArgumentException e) {
            LOG.warn(e.getMessage());
        }
    }

    private static String parseClassMapType(String output) {
        final Optional<String> operator = ParsingUtils.parseField(output, 0, TermReader.CLASS_LINES::matcher,
            matcher -> matcher.group("name"));
        return operator.orElse("");
    }
}