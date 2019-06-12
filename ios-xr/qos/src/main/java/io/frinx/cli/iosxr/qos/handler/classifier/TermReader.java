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
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TermReader implements CliConfigListReader<Term, TermKey, TermBuilder> {

    /**
     * IOS XR devices are configured to store class-maps as "match-all" or "match-any".
     * XR supports show command where the "match-all" or "match-any" piece is omitted (show run class-map a),
     * but our CliReader does not have this functionality.
     * It cannot find the right class-map without calling the right term: match-all or match-any (show run class-map
     * matcher-any a).
     * We do not store information about this term, so we must call the both commands: SH_TERMS_ALL and SH_TERMS_ANY.
     */
    static final String SH_TERMS_ALL = "show running-config class-map match-all %s";
    static final String SH_TERMS_ANY = "show running-config class-map match-any %s";
    private static final Pattern CLASS_TYPE_LINE = Pattern.compile("class-map match-(?<type>.+) (?<name>.+)");
    private static final Pattern MATCH_LINE = Pattern.compile("match (?<condition>.+)");

    private Cli cli;

    public TermReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<TermKey> getAllIds(@Nonnull InstanceIdentifier<Term> instanceIdentifier, @Nonnull ReadContext
            readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(Classifier.class)
                .getName();
        String output = blockingRead(f(SH_TERMS_ALL, name), cli, instanceIdentifier, readContext);

        // class-default will always have only one term, let it be "all"
        // do not read anything, not needed
        if (name.contains(ClassifierReader.DEFAULT_CLASS_SUFFIX)) {
            return Lists.newArrayList(new TermKey(ClassMapType.MATCH_ALL.getStringValue()));
        }

        if (output.equals("")) {
            output = blockingRead(f(SH_TERMS_ANY, name), cli, instanceIdentifier, readContext);
        }
        return getTermKeys(output);
    }

    @VisibleForTesting
    public static List<TermKey> getTermKeys(String output) {
        ClassMapType mapType = parseClassMapType(output);
        if (mapType == null) {
            // this can happen when putting new class-map
            return Collections.emptyList();
        }

        // if class-map is of type match-all, all the conditions need to be in one term
        if (ClassMapType.MATCH_ALL.equals(mapType)) {
            return Lists.newArrayList(new TermKey(ClassMapType.MATCH_ALL.getStringValue()));
        }

        // if class-map is of type match-any, all the match statements need to have a separate term
        // count terms from 1, the number represents the line where the match statement is in config
        long found = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(MATCH_LINE::matcher)
                .filter(Matcher::matches)
                .count();
        return LongStream.rangeClosed(1, found)
                .mapToObj(i -> new TermKey(String.valueOf(i)))
                .collect(Collectors.toList());
    }

    private static ClassMapType parseClassMapType(String output) {
        return ParsingUtils.parseField(output, 0, CLASS_TYPE_LINE::matcher,
            matcher -> matcher.group("type"))
                .map(ClassMapType::parseOutput)
                .orElse(null);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Term> instanceIdentifier, @Nonnull TermBuilder
            termBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Term.class)
                .getId();
        termBuilder.setId(id);
        termBuilder.setConfig(new ConfigBuilder().setId(id)
                .build());
    }
}
