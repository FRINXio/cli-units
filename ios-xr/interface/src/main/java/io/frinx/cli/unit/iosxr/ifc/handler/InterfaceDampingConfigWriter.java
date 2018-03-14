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

package io.frinx.cli.unit.iosxr.ifc.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.damping.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceDampingConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String NO_DAMPENING = "no dampening";
    private static final String DEFAULT_DAMPENING_COMMAND = "dampening";
    private static final String WRITE_CURR_ATTR = "interface {$ifcName}\n" +
            "{$dampConfCommand}\n" +
            "exit";

    private static final String DELETE_CURR_ATTR = "interface {$ifcName}\n" +
            NO_DAMPENING + "\n" +
            "exit";

    public InterfaceDampingConfigWriter(Cli cli) {
        this.cli = cli;
    }


    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        validateConfig(dataAfter);

        String dampConfCommand = getDampeningCommand(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter, fT(WRITE_CURR_ATTR,
                "ifcName", ifcName,
                "dampConfCommand", dampConfCommand));
    }

    private static String getDampeningCommand(Config dataAfter) {
        if (!dataAfter.isEnabled()) {
            return NO_DAMPENING;
        } else if (dataAfter.getMaxSuppress() != null) {
            return String.format("dampening %s %s %s %s", dataAfter.getHalfLife(), dataAfter.getReuse(),
                    dataAfter.getSuppress(), dataAfter.getMaxSuppress());
        } else if (dataAfter.getHalfLife() != null) {
            return String.format("dampening %s", dataAfter.getHalfLife());
        }

        return DEFAULT_DAMPENING_COMMAND;
    }

    private static void validateConfig(Config dataAfter) {
        Preconditions.checkNotNull(dataAfter.isEnabled(), "Field enabled must be specified in damping.");
        Long halfLife = dataAfter.getHalfLife();
        Long reuseThreshold = dataAfter.getReuse();
        Long suppressThreshold = dataAfter.getSuppress();
        Long maxSuppressTime = dataAfter.getMaxSuppress();

        // check if we have valid parameter combination
        checkParamCombination(halfLife, reuseThreshold, suppressThreshold, maxSuppressTime, dataAfter);

        // check parameter ranges
        checkParamRanges(halfLife, reuseThreshold, suppressThreshold, maxSuppressTime);

        // check if reuse is less than suppress
        Preconditions.checkArgument(suppressThreshold == null || reuseThreshold < suppressThreshold,
                "Reuse threshold is not less than suppress threshold");
    }

    private static void checkParamCombination(Long halfLife, Long reuseThreshold, Long suppressThreshold,
                                              Long maxSuppressTime, Config config) {
        boolean validEmptyCombination = halfLife == null && reuseThreshold == null && suppressThreshold == null
                && maxSuppressTime == null;

        boolean validJustHalfLifeSetCombination = halfLife != null && reuseThreshold == null
                && suppressThreshold == null && maxSuppressTime == null;

        boolean validAllSetCombination = halfLife != null && reuseThreshold != null && suppressThreshold != null
                && maxSuppressTime != null;

        Preconditions.checkArgument(validEmptyCombination || validJustHalfLifeSetCombination
                || validAllSetCombination, "Not valid damping configuration combination %s."
                + "Valid damping configuration combinations: [ half-life [ reuse suppress max-suppress ] ]", config);
    }

    private static void checkParamRanges(Long halfLife, Long reuseThreshold, Long suppressThreshold,
                                         Long maxSuppressTime) {
        Preconditions.checkArgument(halfLife == null || halfLife >= 1 && halfLife <= 45,
                "Decay half life value %s is not in the range of 1 to 45", halfLife);

        Preconditions.checkArgument(reuseThreshold == null || reuseThreshold >= 1 && reuseThreshold <= 20000,
                "Reuse threshold value % is not in the range of 1 to 20000", reuseThreshold);

        Preconditions.checkArgument(
                suppressThreshold == null || suppressThreshold >= 1 && suppressThreshold <= 20000,
                "Max-suppress threshold value % is not in the range of 1 to 20000",
                suppressThreshold);

        Preconditions.checkArgument(maxSuppressTime == null || maxSuppressTime >= 1 && maxSuppressTime <= 255,
                "Max-suppress-time value %s is not in the range of 1 to 255", maxSuppressTime);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        blockingDeleteAndRead(cli, id, fT(DELETE_CURR_ATTR,
                "ifcName", ifcName));
    }
}
