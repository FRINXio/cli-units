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
import com.google.common.base.Strings;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.MatchCommunityConfigListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.PrefixListConditionsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.rpol.extension.conditions.MatchIpPrefixList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetCommunityOptionType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityInlineConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.conditions.top.BgpConditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.NOADVERTISE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.NOEXPORT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.SETOPERATION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyWriter implements CliWriter<PolicyDefinition> {

    @SuppressWarnings("checkstyle:linelength")
    static final String WRITE_ROUTE_MAP = """
            configure terminal
            route-map {$data.name} {$action} {$sequence}
            {% if ($localPref) %}set local-preference {$localPref}
            {% endif %}{% if ($asnValue) %}set as-path prepend {$asnValue}
            {% endif %}{% if ($prefixList.ip_prefix_list) %}match ip address prefix-list{% loop in $prefixList.ip_prefix_list as $ip_prefix %} {$ip_prefix}{% endloop %}
            {% endif %}{% if ($prefixList.ipv6_prefix_list) %}match ipv6 address prefix-list {$prefixList.ipv6_prefix_list}
            {% endif %}{% if ($origin) %}set origin {$origin}
            {% endif %}{% if ($communitySet) %}match community {$communitySet}
            {% endif %}{% if ($setCommunities) %}set community{% loop in $setCommunities as $community %} {$community}{% endloop %}
            {% endif %}end""";

    static final String UPDATE_STATEMENT =
            """
                    configure terminal
                    route-map {$data.name} {$action} {$sequence}
                    {% if ($localPrefEnable == TRUE) %}set local-preference {$localPref}
                    {% elseif ($localPrefEnable == FALSE) %}no set local-preference
                    {% endif %}{% if ($asnEnable == TRUE) %}set as-path prepend {$asnValue}
                    {% elseif ($asnEnable == FALSE) %}no set as-path prepend {$asnValue}
                    {% endif %}{% if ($ipEnable == TRUE) %}no match ip address
                    match ip address prefix-list{% loop in $ip as $ip_prefix %} {$ip_prefix}{% endloop %}
                    {% elseif ($ipEnable == FALSE) %}no match ip address
                    {% endif %}{% if ($ipv6Enable == TRUE) %}no match ipv6 address
                    match ipv6 address prefix-list {$ipv6}
                    {% elseif ($ipv6Enable == FALSE) %}no match ipv6 address
                    {% endif %}{% if ($originEnable == TRUE) %}set origin {$origin}
                    {% elseif ($originEnable == FALSE) %}no set origin
                    {% endif %}{% if ($communitySetEnable == TRUE) %}no match community
                    match community {$communitySet}
                    {% elseif ($communitySetEnable == FALSE) %}no match community
                    {% endif %}{% if ($setCommunitiesEnable == TRUE) %}no set community
                    set community{% loop in $setCommunities as $community %} {$community}{% endloop %}
                    {% elseif ($setCommunitiesEnable == FALSE) %}no set community
                    {% endif %}end""";

    private static final String DELETE_STATEMENT =
            """
                    configure terminal
                    no route-map {$data.name} {$action} {$sequence}
                    end""";

    private static final String DELETE_ROUTE_MAP =
            """
                    configure terminal
                    no route-map {$data.name}
                    end""";

    private final Cli cli;

    public PolicyWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<PolicyDefinition> instanceIdentifier,
                                       @NotNull PolicyDefinition policyDefinition,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, policyDefinition, writeTemplate(policyDefinition));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<PolicyDefinition> instanceIdentifier,
                                        @NotNull PolicyDefinition dataBefore,
                                        @NotNull PolicyDefinition dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, updateTemplate(dataBefore, dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<PolicyDefinition> instanceIdentifier,
                                        @NotNull PolicyDefinition policyDefinition,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(policyDefinition));
    }

    @VisibleForTesting
    String writeTemplate(PolicyDefinition policyDefinition) {
        List<Statement> statementsList = policyDefinition.getStatements().getStatement();
        StringBuilder commands = new StringBuilder();

        for (Statement statement : statementsList) {
            if (commands.length() > 0) {
                commands.append("\n");
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
                            ? getAsnValue(actions.getAugmentation(Actions2.class).getBgpActions()) : null,
                    "origin", actions != null
                            ? getOrigin(actions.getAugmentation(Actions2.class).getBgpActions()) : null,
                    "communitySet", getCommunitySet(statement),
                    "setCommunities", getSetCommunities(statement),
                    "prefixList", getPrefixList(statement)));
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

        StringBuilder commands = new StringBuilder();

        for (var entry : statementsMapAfter.entrySet()) {
            String statementId = entry.getKey();
            if (!statementsMapBefore.containsKey(statementId)) {
                if (commands.length() > 0) {
                    commands.append("\n");
                }
                // Statement from dataAfter does not exists in dataBefore. Create statement
                Statement statement = entry.getValue();
                BgpActions bgpActionsAugAfter = statement.getActions().getAugmentation(Actions2.class).getBgpActions();

                commands.append(fT(WRITE_ROUTE_MAP, "data", dataAfter,
                        "sequence", statementId,
                        "action", getAction(statementsMapAfter.get(statementId).getConfig()
                                .getAugmentation(PrefixListAug.class)),
                        "aug", statementsMapAfter.get(statementId).getConfig()
                                .getAugmentation(PrefixListAug.class),
                        "localPref", getLocalPref(bgpActionsAugAfter),
                        "asnValue", getAsnValue(bgpActionsAugAfter),
                        "origin", getOrigin(bgpActionsAugAfter),
                        "communitySet", getCommunitySet(statement),
                        "prefixList", getPrefixList(statement)));
            } else {
                // Statement from dataAfter exists in dataBefore. Compare with statement from dataBefore.
                Statement statementBefore = statementsMapBefore.get(statementId);
                Statement statementAfter = statementsMapAfter.get(statementId);

                Actions actionsBefore = statementBefore.getActions();
                Actions actionsAfter = statementAfter.getActions();

                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing
                        .policy.rev170714.policy.statements.top.statements.statement
                        .Config statementConfigBefore = statementBefore.getConfig();
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing
                        .policy.rev170714.policy.statements.top.statements.statement
                        .Config statementConfigAfter = statementAfter.getConfig();

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
                String afterIp = comparePrefixListIp(getPrefixList(statementBefore), getPrefixList(statementAfter));
                // Compare prefix-list ipv6
                String afterIpv6 = comparePrefixListIpv6(getPrefixList(statementBefore), getPrefixList(statementAfter));
                // Compare action
                String afterSetOperation = compareSetOperation(prefixListAugBefore, prefixListAugAfter);
                // Compare origin
                String afterOrigin = compareOrigin(bgpActionsAugBefore, bgpActionsAugAfter);
                // Compare community set
                String afterCommunitySet = compareCommunitySet(statementBefore, statementAfter);
                // Compare set communities
                String afterSetCommunities = compareSetCommunities(statementBefore, statementAfter);

                if (afterPref != null || afterAsn != null || afterIp != null || afterSetOperation != null
                    || afterIpv6 != null || afterOrigin != null || afterCommunitySet != null
                    || afterSetCommunities != null) {
                    if (commands.length() > 0) {
                        commands.append("\n");
                    }
                    commands.append(fT(UPDATE_STATEMENT, "data", dataAfter,
                            "sequence", statementId,
                            "action", statementAfter.getConfig() != null
                                    ? getAction(statementAfter.getConfig()
                                    .getAugmentation(PrefixListAug.class)) : null,
                            "localPrefEnable", afterPref,
                            "localPref", getLocalPref(bgpActionsAugAfter),
                            "asnEnable", afterAsn,
                            "asnValue", "FALSE".equals(afterAsn) ? bgpActionsAugBefore.getSetAsPathPrepend().getConfig()
                                    .getAsn().getValue().toString() : getAsnValue(bgpActionsAugAfter),
                            "ipEnable", afterIp,
                            "ip", getPrefixList(statementAfter).getIpPrefixList(),
                            "ipv6Enable", afterIpv6,
                            "ipv6", getIpv6Prefix(
                                    getPrefixList(statementBefore), getPrefixList(statementAfter), afterIpv6),
                            "action", getSetOperation(prefixListAugBefore, prefixListAugAfter, afterSetOperation),
                            "originEnable", afterOrigin,
                            "origin", getOrigin(bgpActionsAugAfter),
                            "communitySetEnable", afterCommunitySet,
                            "communitySet", getCommunitySet(statementAfter),
                            "prefixList", getPrefixList(statementAfter),
                            "setCommunitiesEnable", afterSetCommunities,
                            "setCommunities", getSetCommunities(statementAfter)
                            ));
                }
            }
        }

        for (var entry : statementsMapBefore.entrySet()) {
            var statementId = entry.getKey();
            if (!statementsMapAfter.containsKey(statementId)) {
                if (commands.length() > 0) {
                    commands.append("\n");
                }
                commands.append(fT(DELETE_STATEMENT, "data", dataAfter,
                        "sequence", statementId,
                        "action", getAction(entry.getValue().getConfig()
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
        if (bgpActionsAug != null
            && bgpActionsAug.getConfig() != null
            && bgpActionsAug.getConfig().getSetLocalPref() != null) {
            return bgpActionsAug.getConfig().getSetLocalPref().toString();
        }
        return null;
    }

    private String getAction(PrefixListAug aug) {
        return aug != null && DENY.class.equals(aug.getSetOperation()) ? "deny" : "permit";
    }

    private String getAsnValue(BgpActions bgpActionsAug) {
        if (bgpActionsAug != null
            && bgpActionsAug.getSetAsPathPrepend() != null
            && bgpActionsAug.getSetAsPathPrepend().getConfig() != null
            && bgpActionsAug.getSetAsPathPrepend().getConfig().getAsn() != null
            && bgpActionsAug.getSetAsPathPrepend().getConfig().getRepeatN() != null) {
            String asnValue = bgpActionsAug.getSetAsPathPrepend().getConfig().getAsn().getValue().toString();
            Short repeatN = bgpActionsAug.getSetAsPathPrepend().getConfig().getRepeatN();
            String repeatedValue = Strings.repeat(asnValue + " ", repeatN);
            return repeatedValue.trim();
        }
        return null;
    }

    private String getOrigin(BgpActions bgpActionsAug) {
        if (bgpActionsAug != null
            && bgpActionsAug.getConfig() != null
            && bgpActionsAug.getConfig().getSetRouteOrigin() != null
            && bgpActionsAug.getConfig().getSetRouteOrigin().getName() != null) {
            return bgpActionsAug.getConfig().getSetRouteOrigin().getName().toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private String getCommunitySet(Statement statement) {
        BgpConditions bgpConditions = getBgpConditions(statement);
        if (bgpConditions != null
            && bgpConditions.getMatchCommunitySet() != null
            && bgpConditions.getMatchCommunitySet().getConfig() != null) {
            List<String> list = bgpConditions.getMatchCommunitySet().getConfig()
                    .getAugmentation(MatchCommunityConfigListAug.class).getCommunitySetList();
            if (list != null) {
                return String.join(" ", list);
            }

        }
        return null;
    }

    private MatchIpPrefixList getPrefixList(Statement statement) {
        BgpConditions bgpConditions = getBgpConditions(statement);
        if (bgpConditions != null && bgpConditions.getAugmentation(PrefixListConditionsAug.class) != null) {
            return bgpConditions.getAugmentation(PrefixListConditionsAug.class).getMatchIpPrefixList();
        }
        return null;
    }

    public List<String> getSetCommunities(Statement statement) {
        BgpActions bgpActions = getBgpActions(statement);
        if (bgpActions != null
            && bgpActions.getSetCommunity() != null
            && bgpActions.getSetCommunity().getConfig() != null
            && bgpActions.getSetCommunity().getInline() != null
            && bgpActions.getSetCommunity().getInline().getConfig() != null
            && bgpActions.getSetCommunity().getInline().getConfig().getCommunities() != null) {
            List<String> setCommunities = new ArrayList<>();

            for (SetCommunityInlineConfig.Communities community : bgpActions.getSetCommunity().getInline().getConfig()
                    .getCommunities()) {
                if (community.getIdentityref() != null) {
                    if (community.getIdentityref().equals(NOEXPORT.class)) {
                        setCommunities.add("no-export");
                    }
                    if (community.getIdentityref().equals(NOADVERTISE.class)) {
                        setCommunities.add("no-advertise");
                    }
                } else {
                    setCommunities.add(String.valueOf(community.getValue()));
                }
            }

            if (bgpActions.getSetCommunity().getConfig().getOptions() == BgpSetCommunityOptionType.ADD) {
                setCommunities.add("additive");
            }

            return setCommunities;
        }
        return null;
    }

    private BgpConditions getBgpConditions(Statement statement) {
        if (statement.getConditions() != null
            && statement.getConditions().getAugmentation(Conditions2.class) != null) {
            return statement.getConditions().getAugmentation(Conditions2.class).getBgpConditions();
        }
        return null;
    }

    private BgpActions getBgpActions(Statement statement) {
        if (statement.getActions() != null
            && statement.getActions().getAugmentation(Actions2.class) != null) {
            return statement.getActions().getAugmentation(Actions2.class).getBgpActions();
        }
        return null;
    }

    private String compareSetLocalPref(BgpActions bgpActionsAugBefore, BgpActions bgpActionsAugAfter) {
        String localPrefBefore = getLocalPref(bgpActionsAugBefore);
        String localPrefAfter = getLocalPref(bgpActionsAugAfter);
        return compareObjects(localPrefBefore, localPrefAfter);
    }

    private String compareAsn(BgpActions bgpActionsAugBefore, BgpActions bgpActionsAugAfter) {
        String asnBefore = getAsnValue(bgpActionsAugBefore);
        String asnAfter = getAsnValue(bgpActionsAugAfter);
        return compareObjects(asnBefore, asnAfter);
    }

    private String comparePrefixListIp(MatchIpPrefixList prefixListAugAugBefore, MatchIpPrefixList prefixListAugAfter) {
        if (prefixListAugAugBefore != null && prefixListAugAfter != null) {
            List<String> ipPrefixListBefore = prefixListAugAugBefore.getIpPrefixList();
            List<String> ipPrefixListAfter = prefixListAugAfter.getIpPrefixList();
            return compareObjects(ipPrefixListBefore, ipPrefixListAfter);
        }
        return null;
    }

    private String comparePrefixListIpv6(MatchIpPrefixList prefixListAugAugBefore,
                                         MatchIpPrefixList prefixListAugAfter) {
        if (prefixListAugAugBefore != null && prefixListAugAfter != null) {
            String ipv6PrefixListBefore = prefixListAugAugBefore.getIpv6PrefixList();
            String ipv6PrefixListAfter = prefixListAugAfter.getIpv6PrefixList();
            return compareObjects(ipv6PrefixListBefore, ipv6PrefixListAfter);
        }
        return null;
    }

    private String compareSetOperation(PrefixListAug prefixListAugBefore, PrefixListAug prefixListAugAfter) {
        if (prefixListAugBefore != null && prefixListAugAfter != null) {
            Class<? extends SETOPERATION> setOperationBefore = prefixListAugBefore.getSetOperation();
            Class<? extends SETOPERATION> setOperationAfter = prefixListAugAfter.getSetOperation();
            return compareObjects(setOperationBefore, setOperationAfter);
        }
        return null;
    }

    private String getIpv6Prefix(MatchIpPrefixList prefixListAugBefore,
                                 MatchIpPrefixList prefixListAugAfter, String ipv6) {
        if (prefixListAugBefore != null && prefixListAugAfter != null) {
            return "FALSE".equals(ipv6)
                    ? prefixListAugBefore.getIpv6PrefixList()
                    : prefixListAugAfter.getIpv6PrefixList();
        }
        return null;
    }

    private String getSetOperation(PrefixListAug prefixListAugBefore, PrefixListAug prefixListAugAfter, String action) {
        if (prefixListAugBefore != null && prefixListAugAfter != null) {
            return "FALSE".equals(action)
                    ? prefixListAugBefore.getSetOperation() == PERMIT.class ? "permit" : "deny"
                    : prefixListAugAfter.getSetOperation() == PERMIT.class ? "permit" : "deny";
        }
        return null;
    }

    private String compareOrigin(BgpActions bgpActionsAugBefore, BgpActions bgpActionsAugAfter) {
        String originBefore = getOrigin(bgpActionsAugBefore);
        String originAfter = getOrigin(bgpActionsAugAfter);
        return compareObjects(originBefore, originAfter);
    }

    private String compareCommunitySet(Statement statementBefore, Statement statementAfter) {
        String communitySetBefore = getCommunitySet(statementBefore);
        String communitySetAfter = getCommunitySet(statementAfter);
        return compareObjects(communitySetBefore, communitySetAfter);
    }

    private String compareSetCommunities(Statement statementBefore, Statement statementAfter) {
        List<String> setCommunitiesBefore = getSetCommunities(statementBefore);
        List<String> setCommunitiesAfter = getSetCommunities(statementAfter);
        return compareObjects(setCommunitiesBefore, setCommunitiesAfter);
    }

    private String compareObjects(Object objectBefore, Object objectAfter) {
        if (!Objects.equals(objectBefore, objectAfter)) {
            return objectAfter != null ? Chunk.TRUE : "FALSE";
        }
        return null;
    }
}