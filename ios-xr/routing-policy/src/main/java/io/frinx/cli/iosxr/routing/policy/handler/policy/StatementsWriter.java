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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliFormatter;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.math.NumberUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementsWriter implements CliWriter<Statements> {

    private final Cli cli;

    public StatementsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Statements> id,
                                       @Nonnull Statements dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        List<Statement> statements = dataAfter.getStatement();
        if (statements == null) {
            return;
        }

        List<Statement> sortedStatements = sortByNames(dataAfter.getStatement());
        validateNames(sortedStatements);

        String rpName = id.firstKeyOf(PolicyDefinition.class).getName();
        blockingWriteAndRead(cli, id, dataAfter,
                processStatements(sortedStatements, rpName, this));
    }

    @VisibleForTesting
    static List<Statement> sortByNames(List<Statement> statements) {
        return statements.stream()
                .peek(s -> Preconditions.checkArgument(NumberUtils.isCreatable(s.getName()),
                        "Statement name must be a number but was " + s.getName()))
                .sorted(Comparator.comparingInt(s -> Integer.valueOf(s.getName())))
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    static void validateNames(List<Statement> sortedStatements) {
        IntStream.range(0, sortedStatements.size())
                .forEachOrdered(i -> {
                    String desiredName = String.valueOf(i + 1);
                    Preconditions.checkArgument(desiredName.equals(sortedStatements.get(i).getName()),
                            "Missing statement with name " + desiredName);
                });
    }

    private static final String POLICY_TEMPLATE = "route-policy {$policy}\n" +
            "{$statements|join(\n)}" +
            "\n" +
            "end-policy";

    // Spaces in the template intentional to produce output comparable to xr
    private static final String STATEMENT_TEMPLATE = "{% if ($conditions) %}" +
            "{.if ($wasIf)}  else{.else}  {.endif}" +
            "if {$conditions|join( and )} then\n" +
            "    {$actions|join(\n    )}" +
            "{% elseif ($actions) %}" +
            "{.if ($wasIf)}  else\n    {$actions|join(\n    )}{.else}  {$actions|join(\n  )}{.endif}" +
            "\n" +
            "{% else %}" +
            "{.if ($wasIf)}  endif\n{.endif}" +
            "{% endif %}";

    @VisibleForTesting
    static String processStatements(List<Statement> statements, String rpName, CliFormatter format) {
        boolean wasIf = false;
        boolean wasElse = false;

        List<String> statementStrings = new ArrayList<>();
        for (Statement statement : statements) {
            List<String> conditions = ConditionRenderer.renderConditions(statement.getConditions());
            List<String> actions = ActionsRenderer.renderActions(statement.getActions(), statement.getConditions());

            if(actions.isEmpty())
                continue;

            statementStrings.add(format.fT(STATEMENT_TEMPLATE,
                    "wasIf", wasIf ? true : null,
                    "conditions", !conditions.isEmpty() ? conditions : null,
                    "actions", actions));

            wasElse = wasIf && conditions.isEmpty();
            wasIf = !conditions.isEmpty();
        }
        // Do one more empty statement, just to close previous if block if there was any
        statementStrings.add(format.fT(STATEMENT_TEMPLATE,
                "wasIf", wasIf || wasElse ? true : null,
                "conditions", null,
                "actions", null));

        return format.fT(POLICY_TEMPLATE,
                "policy", rpName,
                "statements", statementStrings);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Statements> id,
                                        @Nonnull Statements dataBefore,
                                        @Nonnull Statements dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Statements> id,
                                        @Nonnull Statements dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        // open and close the policy, this removes its body (statements)
        String rpName = id.firstKeyOf(PolicyDefinition.class).getName();
        blockingDeleteAndRead(cli, id,
                fT(POLICY_TEMPLATE,
                        "policy", rpName,
                        "statements", Collections.emptyList()));
    }
}
