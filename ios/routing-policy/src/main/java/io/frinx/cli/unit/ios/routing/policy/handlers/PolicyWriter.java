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

package io.frinx.cli.unit.ios.routing.policy.handlers;

import com.google.common.annotations.VisibleForTesting;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.SetAsPathPrepend;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyWriter implements CliWriter<PolicyDefinition> {

    static final String WRITE_ROUTE_MAP =
            "configure terminal\n"
            + "route-map {$data.name} {$action} {$sequence}\n"
            + "{% if ($localPref) %}set local-preference {$localPref}\n{% endif %}"
            + "{% if ($asnValue) %}set as-path prepend {$asnValue}\n{% endif %}"
            + "{% if ($aug.ip_prefix_list) %}match ip address prefix-list"
            + "{% loop in $aug.ip_prefix_list as $ip_prefix %} {$ip_prefix}{% endloop %}\n{% endif %}"
            + "{% if ($aug.ipv6_prefix_list) %}match ipv6 address prefix-list {$aug.ipv6_prefix_list}\n{% endif %}"
            + "end";

    static final String UPDATE_STATEMENT =
            "configure terminal\n"
            + "route-map {$data.name} {$action} {$sequence}\n"
            + "{% if ($localPrefEnable == TRUE) %}set local-preference {$localPref}\n"
            + "{% elseif ($localPrefEnable == FALSE) %}no set local-preference\n{% endif %}"
            + "{% if ($asnEnable == TRUE) %}set as-path prepend {$asnValue}\n"
            + "{% elseif ($asnEnable == FALSE) %}no set as-path prepend {$asnValue}\n{% endif %}"
            + "{% if ($ipEnable == TRUE) %}no match ip address\nmatch ip address prefix-list"
            + "{% loop in $ip as $ip_prefix %} {$ip_prefix}{% endloop %}\n"
            + "{% elseif ($ipEnable == FALSE) %}no match ip address\n{% endif %}"
            + "{% if ($ipv6Enable == TRUE) %}no match ipv6 address\nmatch ipv6 address prefix-list {$ipv6}\n"
            + "{% elseif ($ipv6Enable == FALSE) %}no match ipv6 address\n{% endif %}"
            + "end";

    private static final String DELETE_STATEMENT =
            "configure terminal\n"
            + "no route-map {$data.name} {$action} {$sequence}\n"
            + "end";

    private static final String DELETE_ROUTE_MAP =
            "configure terminal\n"
            + "no route-map {$data.name}\n"
            + "end";

    private final Cli cli;

    public PolicyWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<PolicyDefinition> instanceIdentifier,
                                       @Nonnull PolicyDefinition policyDefinition,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, policyDefinition, writeTemplate(policyDefinition));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<PolicyDefinition> instanceIdentifier,
                                        @Nonnull PolicyDefinition dataBefore,
                                        @Nonnull PolicyDefinition dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, updateTemplate(dataBefore, dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<PolicyDefinition> instanceIdentifier,
                                        @Nonnull PolicyDefinition policyDefinition,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(policyDefinition));
    }

    @VisibleForTesting
    String writeTemplate(PolicyDefinition policyDefinition) {
        List<Statement> statementsList = policyDefinition.getStatements().getStatement();
        StringBuffer commands = new StringBuffer();

        for (Statement statement : statementsList) {
            if (commands.length() > 0) {
                commands.append("/n");
            }
            Actions actions = statement.getActions();
            commands.append(fT(WRITE_ROUTE_MAP, "data", policyDefinition,
                    "sequence", statement.getName(),
                    "aug", statement.getConfig() != null
                            ? statement.getConfig().getAugmentation(PrefixListAug.class) : null,
                    "action", statement.getConfig() != null
                            ? getAction(statement.getConfig().getAugmentation(PrefixListAug.class)) : null,
                    "localPref", actions != null
                            ? getLocalPref(actions.getAugmentation(Actions2.class).getBgpActions()) : null,
                    "asnValue", actions != null
                            ? getAsnValue(actions.getAugmentation(Actions2.class).getBgpActions()) : null));
        }
        return commands.toString();
    }

    @VisibleForTesting
    String updateTemplate(PolicyDefinition dataBefore, PolicyDefinition dataAfter) {
        List<Statement> statementsBefore = dataBefore.getStatements().getStatement();
        List<Statement> statementsAfter = dataAfter.getStatements().getStatement();
        Map<String, Statement> statementsMapBefore = new HashMap<>();
        Map<String, Statement> statementsMapAfter = new HashMap<>();

        statementsBefore.forEach(s -> {
            statementsMapBefore.put(s.getName(), s);
        });
        statementsAfter.forEach(s -> {
            statementsMapAfter.put(s.getName(), s);
        });

        StringBuffer commands = new StringBuffer();

        for (String statementId : statementsMapAfter.keySet()) {
            if (!statementsMapBefore.containsKey(statementId)) {
                if (commands.length() > 0) {
                    commands.append("\n");
                }
                // Statement from dataAfter does not exists in dataBefore. Create statement
                BgpActions bgpActionsAugAfter = statementsMapAfter.get(statementId).getActions()
                        .getAugmentation(Actions2.class).getBgpActions();



                commands.append(fT(WRITE_ROUTE_MAP, "data", dataAfter,
                        "sequence", statementId,
                        "action", getAction(statementsMapAfter.get(statementId).getConfig()
                                .getAugmentation(PrefixListAug.class)),
                        "aug", statementsMapAfter.get(statementId).getConfig()
                                .getAugmentation(PrefixListAug.class),
                        "localPref", getLocalPref(bgpActionsAugAfter),
                        "asnValue", getAsnValue(bgpActionsAugAfter)));
            } else {
                // Statement from dataAfter exists in dataBefore. Compare with statement from dataBefore.
                Actions actionsBefore = statementsMapBefore.get(statementId).getActions();
                Actions actionsAfter = statementsMapAfter.get(statementId).getActions();

                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing
                        .policy.rev170714.policy.statements.top.statements.statement
                        .Config statementConfigBefore = statementsMapBefore.get(statementId).getConfig();
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing
                        .policy.rev170714.policy.statements.top.statements.statement
                        .Config statementConfigAfter = statementsMapAfter.get(statementId).getConfig();

                BgpActions bgpActionsAugBefore = actionsBefore != null
                        ? actionsBefore.getAugmentation(Actions2.class).getBgpActions() : null;
                BgpActions bgpActionsAugAfter = actionsAfter != null
                        ? actionsAfter.getAugmentation(Actions2.class).getBgpActions() : null;

                PrefixListAug prefixListAugBefore = statementConfigBefore != null
                        ? statementConfigBefore.getAugmentation(PrefixListAug.class) : null;

                PrefixListAug prefixListAugAfter = statementConfigAfter != null
                        ? statementConfigAfter.getAugmentation(PrefixListAug.class) : null;

                // Compare setLocalPreferences
                String afterPref = compareSetLocalPref(bgpActionsAugBefore, bgpActionsAugAfter);
                // Compare asn
                String afterAsn = compareAsn(bgpActionsAugBefore, bgpActionsAugAfter);
                // Compare prefix-list ip
                String afterIp = comparePrefixListIp(prefixListAugBefore, prefixListAugAfter);
                // Compare prefix-list ipv6
                String afterIpv6 = comparePrefixListIpv6(prefixListAugBefore, prefixListAugAfter);
                // Compare action
                String afterSetOperation = compareSetOperation(prefixListAugBefore, prefixListAugAfter);

                if (afterPref != null || afterAsn != null || afterIp != null || afterSetOperation != null
                        || afterIpv6 != null) {
                    if (commands.length() > 0) {
                        commands.append("\n");
                    }
                    commands.append(fT(UPDATE_STATEMENT, "data", dataAfter,
                            "sequence", statementId,
                            "action", statementsMapAfter.get(statementId).getConfig() != null
                                    ? getAction(statementsMapAfter.get(statementId).getConfig()
                                    .getAugmentation(PrefixListAug.class)) : null,
                            "localPrefEnable", afterPref,
                            "localPref", getLocalPref(bgpActionsAugAfter),
                            "asnEnable", afterAsn,
                            "asnValue", "FALSE".equals(afterAsn) ? bgpActionsAugBefore.getSetAsPathPrepend().getConfig()
                                    .getAsn().getValue().toString() : getAsnValue(bgpActionsAugAfter),
                            "ipEnable", afterIp,
                            "ip", prefixListAugAfter.getIpPrefixList(),
                            "ipv6Enable", afterIpv6,
                            "ipv6", getIpv6Prefix(prefixListAugBefore, prefixListAugAfter, afterIpv6),
                            "action", getSetOperation(prefixListAugBefore, prefixListAugAfter, afterSetOperation)
                            ));
                }
            }
        }

        for (String statementId : statementsMapBefore.keySet()) {
            if (!statementsMapAfter.keySet().contains(statementId)) {
                if (commands.length() > 0) {
                    commands.append("\n");
                }
                commands.append(fT(DELETE_STATEMENT, "data", dataAfter,
                        "sequence", statementId,
                        "action", getAction(statementsMapBefore.get(statementId).getConfig()
                                .getAugmentation(PrefixListAug.class))));
            }
        }
        return commands.toString();
    }

    @VisibleForTesting
    String deleteTemplate(PolicyDefinition policyDefinition) {
        return fT(DELETE_ROUTE_MAP, "data", policyDefinition);
    }

    private String getLocalPref(BgpActions bgpActionsAug) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions
                .Config config = bgpActionsAug != null ? bgpActionsAug.getConfig() : null;

        return config != null ? config.getSetLocalPref().toString() : null;
    }

    private String getAction(PrefixListAug aug) {
        if (aug != null) {
            if (DENY.class.equals(aug.getSetOperation())) {
                return "deny";
            }
        }
        return "permit";
    }

    private String getAsnValue(BgpActions bgpActionsAug) {
        SetAsPathPrepend setAsPathPrepend = bgpActionsAug != null ? bgpActionsAug.getSetAsPathPrepend() : null;
        Config config =  setAsPathPrepend != null ? setAsPathPrepend.getConfig() : null;

        if (config != null) {
            String asnValue = config.getAsn().getValue().toString();
            Short repeatN = config.getRepeatN();

            String resultAsnValue = asnValue;

            while (repeatN != 1) {
                resultAsnValue = resultAsnValue + " " + asnValue;
                repeatN--;
            }
            return resultAsnValue;
        }
        return null;
    }

    private String compareSetLocalPref(BgpActions bgpActionsAugBefore, BgpActions bgpActionsAugAfter) {
        String localPrefBefore = getLocalPref(bgpActionsAugBefore);
        String localPrefAfter = getLocalPref(bgpActionsAugAfter);

        if (!Objects.equals(localPrefBefore, localPrefAfter)) {
            if (localPrefAfter != null) {
                return Chunk.TRUE;
            } else {
                return "FALSE";
            }
        }
        return null;
    }

    private String compareAsn(BgpActions bgpActionsAugBefore, BgpActions bgpActionsAugAfter) {
        String asnBefore = getAsnValue(bgpActionsAugBefore);
        String asnAfter = getAsnValue(bgpActionsAugAfter);

        if (!Objects.equals(asnBefore, asnAfter)) {
            if (asnAfter != null) {
                return Chunk.TRUE;
            } else {
                return "FALSE";
            }
        }
        return null;
    }

    private String comparePrefixListIp(PrefixListAug prefixListAugAugBefore, PrefixListAug prefixListAugAfter) {
        if (prefixListAugAugBefore != null && prefixListAugAfter != null) {
            if (!Objects.equals(prefixListAugAugBefore.getIpPrefixList(), prefixListAugAfter.getIpPrefixList())) {
                if (prefixListAugAfter.getIpPrefixList() != null) {
                    return Chunk.TRUE;
                } else {
                    return "FALSE";
                }
            }
        }
        return null;
    }

    private String comparePrefixListIpv6(PrefixListAug prefixListAugAugBefore, PrefixListAug prefixListAugAfter) {
        if (prefixListAugAugBefore != null && prefixListAugAfter != null) {
            if (!Objects.equals(prefixListAugAugBefore.getIpv6PrefixList(),
                    prefixListAugAfter.getIpv6PrefixList())) {
                if (prefixListAugAfter.getIpv6PrefixList() != null) {
                    return Chunk.TRUE;
                } else {
                    return "FALSE";
                }
            }
        } else if (prefixListAugAfter == null && prefixListAugAugBefore != null) {
            return "FALSE";
        }
        return null;
    }

    private String compareSetOperation(PrefixListAug prefixListAugAugBefore, PrefixListAug prefixListAugAfter) {
        if (prefixListAugAugBefore != null && prefixListAugAfter != null) {
            if (!Objects.equals(prefixListAugAugBefore.getSetOperation(), prefixListAugAfter.getSetOperation())) {
                if (prefixListAugAfter.getSetOperation() != null) {
                    return Chunk.TRUE;
                } else {
                    return "FALSE";
                }
            }
        }
        return null;
    }

    private String getIpv6Prefix(PrefixListAug prefixListAugBefore, PrefixListAug prefixListAugAfter, String ipv6) {
        if (prefixListAugBefore != null && prefixListAugAfter != null) {
            return "FALSE".equals(ipv6) ? prefixListAugBefore.getIpv6PrefixList() :
                    prefixListAugAfter.getIpv6PrefixList();
        }
        else {
            return null;
        }
    }

    private String getSetOperation(PrefixListAug prefixListAugBefore, PrefixListAug prefixListAugAfter, String action) {
        if (prefixListAugBefore != null && prefixListAugAfter != null) {
            return "FALSE".equals(action) ? prefixListAugBefore.getSetOperation() == PERMIT.class ? "permit" : "deny" :
                    prefixListAugAfter.getSetOperation() == PERMIT.class ? "permit" : "deny";
        }
        else {
            return null;
        }
    }

}