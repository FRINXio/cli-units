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
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TermReader implements CliConfigListReader<Term, TermKey, TermBuilder> {

    public static final String SH_TERMS = "display current-configuration | section traffic classifier %s";
    public static final Pattern CLASS_LINES = Pattern.compile("if-match (?<name>\\S+).+");

    private Cli cli;

    public TermReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<TermKey> getAllIds(@NotNull InstanceIdentifier<Term> instanceIdentifier,
                                   @NotNull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        final String classOutput = blockingRead(f(SH_TERMS, name), cli, instanceIdentifier, readContext);

        // class-default will always have only one term, let it be "all"
        // do not read anything, not needed
        if (name.contains(ClassifierReader.DEFAULT_CLASS_SUFFIX)) {
            return Lists.newArrayList(new TermKey(ClassMapType.MATCH_ALL.getStringValue()));
        }

        return getTermKeys(classOutput);
    }

    @VisibleForTesting
    public static List<TermKey> getTermKeys(String classOutput) {
        final String mapType = parseClassMapType(classOutput);

        final Optional<String> operator = getOperations(classOutput, mapType);
        if (operator.isPresent()) {
            String operators = operator.get();
            final int found = operators.split(" ").length;
            return LongStream.rangeClosed(1, found)
                    .mapToObj(i -> new TermKey(String.valueOf(i)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();

    }

    private static String parseClassMapType(String output) {
        final Optional<String> operator = ParsingUtils.parseField(output, 0, CLASS_LINES::matcher,
            matcher -> matcher.group("name"));
        return operator.orElse("");
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Term> instanceIdentifier,
                                      @NotNull TermBuilder termBuilder, @NotNull ReadContext readContext) {
        final String id = instanceIdentifier.firstKeyOf(Term.class).getId();
        termBuilder.setId(id);
        termBuilder.setConfig(new ConfigBuilder().setId(id).build());
    }

    private static Pattern parseOperation(final String name) {
        final String regex = String.format("if-match %s (?<type>\\S+.+)", name);
        return Pattern.compile(regex);
    }

    static Optional<String> getOperations(String classOutput, String mapType) {
        return ParsingUtils.parseField(classOutput, 0,
            parseOperation(mapType)::matcher,
            matcher -> matcher.group("type"));
    }
}