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
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.qos.Util;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
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

    public static final String SH_TERMS = "show class-map %s";
    private static final Pattern MATCH_LINE = Pattern.compile("Match (?<condition>.+)");

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
        final String output = Util.deleteBrackets(classOutput);
        final ClassMapType mapType = parseClassMapType(output);

        // this can happen when putting new class-map
        if (mapType == null) {
            return Collections.emptyList();
        }

        // if class-map is of type match-all, all the conditions need to be in one term
        if (ClassMapType.MATCH_ALL.equals(mapType)) {
            return Lists.newArrayList(new TermKey(ClassMapType.MATCH_ALL.getStringValue()));
        }

        // if class-map is of type match-any, all the match statements need to have a separate term
        // count terms from 1, the number represents the line where the match statement is in config
        final long found = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(MATCH_LINE::matcher)
                .filter(Matcher::matches)
                .count();

        return LongStream.rangeClosed(1, found)
                .mapToObj(i -> new TermKey(String.valueOf(i)))
                .collect(Collectors.toList());
    }

    private static ClassMapType parseClassMapType(String output) {
        return ParsingUtils.parseField(output, 0, ClassifierReader.CLASS_LINE::matcher,
            matcher -> matcher.group("type"))
            .map(ClassMapType::parseOutput)
            .orElse(null);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Term> instanceIdentifier,
                                      @NotNull TermBuilder termBuilder, @NotNull ReadContext readContext) {
        final String id = instanceIdentifier.firstKeyOf(Term.class).getId();
        termBuilder.setId(id);
        termBuilder.setConfig(new ConfigBuilder().setId(id).build());
    }
}