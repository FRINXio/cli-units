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

package io.frinx.cli.iosxr.routing.policy.handler.policy;

import static io.frinx.cli.iosxr.routing.policy.handler.policy.ActionsParser.EMPTY_ACTIONS;
import static io.frinx.cli.iosxr.routing.policy.handler.policy.ActionsParser.parseActions;
import static io.frinx.cli.iosxr.routing.policy.handler.policy.ConditionParser.EMPTY_CONDITIONS;
import static io.frinx.cli.iosxr.routing.policy.handler.policy.ConditionParser.parseConditions;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.StatementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementsReader implements CliConfigReader<Statements, StatementsBuilder> {

    private static final String SH_PREFIX_SET = "do show running-config route-policy %s";

    private Cli cli;

    public StatementsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Statements> id,
                                      @Nonnull StatementsBuilder statementsBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        PolicyDefinitionKey policyDefinitionKey = id.firstKeyOf(PolicyDefinition.class);
        String output = blockingRead(f(SH_PREFIX_SET, policyDefinitionKey.getName()), cli, id, readContext);
        parseOutput(output, statementsBuilder);
    }

    static void parseOutput(String output, StatementsBuilder statementsBuilder) {
        // Process the lines as a stream and handle one by one with the collector
        List<Statement> statements = ParsingUtils.NEWLINE.splitAsStream(output)
                // Only top level and 1 level of indent is supported (e.g. top level ifs)
                .filter(StatementsReader::isIndentSupported)
                .collect(new StatementCollector());

        if (!statements.isEmpty()) {
            statementsBuilder.setStatement(statements);
        }
    }

    private static final String INDENT = "  ";
    private static final int INDENT_LEVELS_SUPPORT = 2;

    private static boolean isIndentSupported(String line) {
        return IntStream.range(1, INDENT_LEVELS_SUPPORT)
                .mapToObj(i -> Strings.repeat(INDENT, i))
                .anyMatch(line::startsWith);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder,
                      @Nonnull Statements statements) {
        ((PolicyDefinitionBuilder) builder).setStatements(statements);
    }

    /**
     * Collects lines of route-policy and produces a statement list using StatementsAccumulator
     */
    private static class StatementCollector implements Collector<String, StatementsAccumulator, List<Statement>> {

        @Override
        public Supplier<StatementsAccumulator> supplier() {
            return StatementsAccumulator::new;
        }

        @Override
        public BiConsumer<StatementsAccumulator, String> accumulator() {
            return StatementsAccumulator::accept;
        }

        @Override
        public BinaryOperator<StatementsAccumulator> combiner() {
            return StatementsAccumulator::merge;
        }

        @Override
        public Function<StatementsAccumulator, List<Statement>> finisher() {
            return StatementsAccumulator::build;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }

    /**
     * Stateful accumulator of route-policy lines.
     */
    private static class StatementsAccumulator {

        // FIXME what about or in conditions
        // FIXME what about multiple conditions of the same type in a single if ? destination in XX and destination in YY
        // Right now, parsing is best effor, we parse what we can and ignore the rest
        // ... but the result openconfig configuration might not conform to reality

        String IF = INDENT + "if";
        String ELSEIF = INDENT + "elseif";
        String ELSE = INDENT + "else";
        String ENDIF = INDENT + "endif";

        private final List<StatementBuilder> builders = Lists.newArrayList();

        void accept(String line) {
            StatementBuilder currentBuilder = builders.isEmpty() ? newBuilder() : currentBuilder();

            // Conditional statements, supported only on top level
            if (line.startsWith(IF) || line.startsWith(ELSEIF) || line.startsWith(ELSE)) {
                line = line.trim();
                currentBuilder = newBuilder();
                Conditions conds = parseConditions(line);

                // Do not set empty
                if (!EMPTY_CONDITIONS.equals(conds)) {
                    currentBuilder.setConditions(conds);
                }
            } else if (line.startsWith(ENDIF)) {
                // Close builder
                newBuilder();
            } else {
                // Action statements supported on top level and 1 nested level
                line = line.trim();
                Actions actions = currentBuilder.getActions();
                ActionsBuilder actionsBuilder = actions == null ? new ActionsBuilder() : new ActionsBuilder(actions);
                parseActions(actionsBuilder, line);
                Actions build = actionsBuilder.build();

                // Do not set empty
                if (!EMPTY_ACTIONS.equals(build)) {
                    currentBuilder.setActions(build);
                }
            }
        }

        private StatementBuilder newBuilder() {
            StatementBuilder stmtBuilder = new StatementBuilder();
            builders.add(stmtBuilder);
            return stmtBuilder;
        }

        private StatementBuilder currentBuilder() {
            return Iterables.getLast(builders);
        }

        StatementsAccumulator merge(StatementsAccumulator other) {
            throw new UnsupportedOperationException("Cannot merge to accumulators, concurrent processing not supported");
        }

        private static final Statement EMPTY_STATEMENT = new StatementBuilder().build();

        List<Statement> build() {
            // Filter out empty statements
            List<Statement> unnamendStatements = builders.stream()
                    .map(StatementBuilder::build)
                    .filter(o -> !EMPTY_STATEMENT.equals(o))
                    .collect(Collectors.toList());

            // Add name based on statement index
            return IntStream.range(0, unnamendStatements.size())
                    .mapToObj(i -> new AbstractMap.SimpleEntry<>(i + 1, unnamendStatements.get(i)))
                    .map(e -> new StatementBuilder(e.getValue())
                            .setName(Integer.toString(e.getKey()))
                            .setKey(new StatementKey(Integer.toString(e.getKey())))
                            .setConfig(new ConfigBuilder()
                                    .setName(Integer.toString(e.getKey()))
                                    .build())
                            .build())
                    .collect(Collectors.toList());
        }
    }

}
