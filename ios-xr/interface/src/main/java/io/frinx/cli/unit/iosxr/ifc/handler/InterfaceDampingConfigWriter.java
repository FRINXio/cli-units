/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.io.yang.damping.rev171024.damping.top.damping.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceDampingConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String NO_DAMPENING = "no dampening";
    private static final String EMPTY_COMMAND = "";

    public InterfaceDampingConfigWriter(Cli cli) {
        this.cli = cli;
    }


    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        validateConfig(dataAfter);

        String dampConfCommand = getDampeningCommand(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("interface %s", ifcName),
                dampConfCommand,
                "commit",
                "end");
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

        return EMPTY_COMMAND;
    }

    private static void validateConfig(Config dataAfter) {
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
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("interface %s", ifcName),
                NO_DAMPENING,
                "commit",
                "end");
    }
}
