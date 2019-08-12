/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanRange;

public final class Vlan {

    public static final Pattern VLAN_ID_LINE = Pattern.compile("vlan (?<id>\\d+)\\s*(name\\s(?<name>\\S+))?");
    private static final Pattern IFC_TAGGED = Pattern.compile("((?<tagged>(no un|un)?tagged|router-interface) "
            + "([a-z]{1,4}\\s(\\d/)?\\d+\\s?)+)");
    private static final Pattern IFC_RANGE = Pattern.compile("(e(the)? \\d/\\d+ to \\d/\\d+\\s?)");
    private static final Pattern IFC_SINGLE = Pattern.compile("(e(the)? \\d/\\d+\\s?)");
    private static final Pattern IFC_VE = Pattern.compile("(ve \\d+\\s?)");
    private static final Pattern TRUNK_RANGE = Pattern.compile("(?<startRange>\\d+)\\.\\.(?<endRange>\\d+)");

    private static final String NO_UNTAGGED = "no untagged";
    private static final String TAGGED = "tagged";
    private static final String UNTAGGED = "untagged";

    private VlanId id;
    private List<SWInterface> interfaces;

    private Vlan(VlanId vlanId, List<SWInterface> allInterfaces) {
        this.id = vlanId;
        interfaces = ImmutableList.copyOf(allInterfaces);
    }

    public VlanId getId() {
        return id;
    }

    public List<SWInterface> getInterfaces() {
        return this.interfaces;
    }

    static void buildConfiguration(@Nonnull String ifcName, @Nonnull ConfigBuilder configBuilder, @Nonnull Vlan vlan) {
        long count = vlan.getInterfaces().stream().filter(sw -> sw.containsInterface(ifcName))
                .filter(sw -> NO_UNTAGGED.equals(sw.getTag()))
                .count();
        if (vlan.getId().getValue() == 1 && count == 0) {
            configBuilder.setAccessVlan(vlan.getId());
            configBuilder.setInterfaceMode(VlanModeType.ACCESS);
        }

        vlan.getInterfaces().stream().filter(sw -> sw.containsInterface(ifcName) && TAGGED.equals(sw.getTag()))
                .forEach(sw -> {
                    if (VlanModeType.ACCESS.equals(configBuilder.getInterfaceMode())
                            && configBuilder.getAccessVlan().getValue() != 1) {
                        configBuilder.setNativeVlan(configBuilder.getAccessVlan());
                    }

                    getTrunkVlans(configBuilder).add(new VlanSwitchedConfig.TrunkVlans(vlan.getId()));
                    configBuilder.setInterfaceMode(VlanModeType.TRUNK);
                    configBuilder.setAccessVlan(null);
                }
        );

        vlan.getInterfaces().stream().filter(sw -> sw.containsInterface(ifcName) && UNTAGGED.equals(sw.getTag()))
                .forEach(sw -> {
                    if (VlanModeType.TRUNK.equals(configBuilder.getInterfaceMode())) {
                        configBuilder.setNativeVlan(vlan.getId());
                    }
                    if (configBuilder.getInterfaceMode() == null
                            || VlanModeType.ACCESS.equals(configBuilder.getInterfaceMode())) {
                        configBuilder.setInterfaceMode(VlanModeType.ACCESS);
                        configBuilder.setAccessVlan(vlan.getId());
                    }
                }
        );
    }

    private static List<VlanSwitchedConfig.TrunkVlans> getTrunkVlans(ConfigBuilder configBuilder) {
        List<VlanSwitchedConfig.TrunkVlans> trunkVlans = configBuilder.getTrunkVlans();
        if (trunkVlans == null) {
            configBuilder.setTrunkVlans(trunkVlans = Lists.newArrayList());
        }
        return trunkVlans;
    }

    static List<VlanSwitchedConfig.TrunkVlans> optimizeTrunkVlans(List<VlanSwitchedConfig.TrunkVlans> trunkVlans) {
        if (trunkVlans == null || trunkVlans.isEmpty()) {
            return trunkVlans;
        }

        trunkVlans.sort(Comparator.comparing(o -> o.getVlanId().getValue()));
        Integer first = trunkVlans.get(0).getVlanId().getValue();
        Integer last = first;
        List<VlanSwitchedConfig.TrunkVlans> newVlans = Lists.newArrayList();
        for (int i = 0; i < trunkVlans.size(); i++) {
            VlanId vlanId = trunkVlans.get(i).getVlanId();
            VlanSwitchedConfig.TrunkVlans nextTrunkVlan = i + 1 < trunkVlans.size() ? trunkVlans.get(i + 1) : null;

            if (nextTrunkVlan != null
                    && vlanId.getValue() + 1 == nextTrunkVlan.getVlanId().getValue()) {
                last = nextTrunkVlan.getVlanId().getValue();
            } else {
                VlanSwitchedConfig.TrunkVlans tmp = first < last
                        ? new VlanSwitchedConfig.TrunkVlans(new VlanRange(String.format("%s..%s", first, last)))
                        : new VlanSwitchedConfig.TrunkVlans(vlanId);
                newVlans.add(tmp);

                first = last = nextTrunkVlan == null
                        ? vlanId.getValue()
                        : nextTrunkVlan.getVlanId().getValue();
            }
        }
        return newVlans;
    }

    static List<VlanSwitchedConfig.TrunkVlans> parseVlanRanges(List<VlanSwitchedConfig.TrunkVlans> trunkVlans) {
        List<VlanSwitchedConfig.TrunkVlans> newTrunkVlans = trunkVlans.stream()
            .filter(tv -> tv.getVlanId() != null)
            .collect(Collectors.toList());

        trunkVlans.stream()
            .filter(tv -> tv.getVlanRange() != null)
            .map(tv -> TRUNK_RANGE.matcher(tv.getVlanRange().getValue()))
            .filter(Matcher::matches)
            .forEach(m -> {
                Integer startRange = Integer.valueOf(m.group("startRange"));
                Integer endRange = Integer.valueOf(m.group("endRange"));
                Preconditions.checkArgument(startRange < endRange,
                    String.format("Range [%s..%s] in trunk must be from lower to higher number", startRange, endRange));

                for (int i = startRange; i <= endRange; i++) {
                    newTrunkVlans.add(new VlanSwitchedConfig.TrunkVlans(new VlanId(i)));
                }
            });

        return newTrunkVlans;
    }

    public static Vlan create(String line) {
        Integer vlanId = ParsingUtils.NEWLINE.splitAsStream(line)
                .map(VLAN_ID_LINE::matcher)
                .filter(Matcher::find)
                .map(m -> m.group("id"))
                .findFirst()
                .map(Integer::valueOf)
                .get();

        return new Vlan(new VlanId(vlanId), findAllInterfaces(line));
    }

    private static List<SWInterface> findAllInterfaces(String line) {
        List<SWInterface> list = Lists.newArrayList();
        findAll(line, IFC_TAGGED, l -> Vlan.parse(l, list));
        return list;
    }

    private static void parse(String tagLine, List<SWInterface> list) {
        findAll(tagLine, IFC_TAGGED, m -> true, m -> m.group(TAGGED), ifc -> parseInterfaces(ifc, tagLine, list));
    }

    private static void parseInterfaces(String tagged, String ifcLine, List<SWInterface> list) {
        findAll(ifcLine, IFC_RANGE, ifc -> list.add(new InterfaceRangeInVlan(tagged, ifc)));
        findAll(ifcLine, IFC_SINGLE, m -> ifcLine.indexOf("to", m.end()) - m.end() != 0, Matcher::group,
            ifc -> list.add(new InterfaceInVlan(tagged, ifc)));
        findAll(ifcLine, IFC_VE, ifc -> list.add(new RouterInterfaceInVlan(tagged, ifc)));
    }

    private static void findAll(String line, Pattern pattern, Consumer<String> consumer) {
        findAll(line, pattern, p -> !p.group().isEmpty(), Matcher::group, consumer);
    }

    private static void findAll(String line, Pattern pattern, Predicate<Matcher> predicate,
                         Function<Matcher, String> extract, Consumer<String> consumer) {
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            if (predicate.test(matcher)) {
                consumer.accept(extract.apply(matcher));
            }
        }
    }
}
