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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.damping.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceDampingConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String NO_DAMPENING = "no dampening";
    private static final String DEFAULT_DAMPENING_COMMAND = "dampening";

    public InterfaceDampingConfigWriter(Cli cli) {
        this.cli = cli;
    }


    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();

        validateConfig(dataAfter);

        String dampConfCommand = getDampeningCommand(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
                f("interface %s", ifcName),
                dampConfCommand,
                "root");
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

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
                                        @NotNull Config dataAfter, @NotNull WriteContext writeContext)
            throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();

        blockingDeleteAndRead(cli, id,
                f("interface %s", ifcName),
                NO_DAMPENING,
                "root");
    }
}