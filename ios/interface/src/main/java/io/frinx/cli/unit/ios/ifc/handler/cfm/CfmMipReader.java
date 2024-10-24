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

package io.frinx.cli.unit.ios.ifc.handler.cfm;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.mip.Level;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.mip.LevelBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.mip.LevelKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmMipReader implements CliConfigListReader<Level, LevelKey, LevelBuilder> {

    private static final Pattern CFM_MIP = Pattern.compile("\\s*ethernet cfm mip level (?<level>.+) vlan (?<vlan>.+)");

    private final Cli cli;

    public CfmMipReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<LevelKey> getAllIds(@NotNull InstanceIdentifier<Level> id,
                                    @NotNull ReadContext ctx) throws ReadFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        final String ifcOutput = blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx);
        return getAllLevels(ifcOutput);
    }

    private List<LevelKey> getAllLevels(String ifcOutput) {
        return ParsingUtils.parseFields(ifcOutput, 0,
            CFM_MIP::matcher,
            m -> Short.valueOf(m.group("level")),
            LevelKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Level> id,
                                      @NotNull LevelBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        final Short level = id.firstKeyOf(Level.class).getLevel();
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        final String ifcOutput = blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx);
        parseLevelMip(level, ifcOutput, builder);
    }

    @VisibleForTesting
    public void parseLevelMip(Short level, String ifcOutput, LevelBuilder builder) {
        String levelLine = String.format("ethernet cfm mip level %d vlan (?<vlan>.+)", level);
        Optional<String> vlanValues = ParsingUtils.parseField(ifcOutput, 0,
            Pattern.compile(levelLine)::matcher,
            matcher -> matcher.group("vlan"));

        if (vlanValues.isPresent()) {
            builder.setLevel(level);
            builder.setVlan(getVlanIds(vlanValues.get()));
        }
    }

    private List<String> getVlanIds(String vlanValues) {
        List<String> vlanIds = new ArrayList<>();
        List<String> vlanGroup = Arrays.asList(vlanValues.split(","));
        vlanGroup.forEach(id -> {
            if (id.contains("-")) {
                String[] indexes = id.split("-");
                for (int i = Integer.parseInt(indexes[0]); i <= Integer.parseInt(indexes[1]); i++) {
                    vlanIds.add(String.valueOf(i));
                }
            } else {
                vlanIds.add(id);
            }
        });
        return vlanIds;
    }
}