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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.statistics.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class InterfaceStatisticsConfigWriter implements CliWriter<Config> {
    private Cli cli;

    static final String MOD_CURR_ATTR = "interface {$ifcName}\n" +
            "{%if ($delete) %}no {%endif%}load-interval {%if(!$delete) %}{$loadInterval.load_interval}{%endif%}\n" +
            "exit";

    public InterfaceStatisticsConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        if (validateConfig(dataAfter)) {
            blockingWriteAndRead(cli, id, dataAfter, fT(MOD_CURR_ATTR,
                    "ifcName", ifcName,
                    "loadInterval", dataAfter));
        }
    }

    private static boolean validateConfig(Config dataAfter) {
        Long loadInterval = dataAfter.getLoadInterval();
        if (loadInterval == null) {
            return false;
        }

        // check range
        Preconditions.checkArgument(loadInterval >= 0 && loadInterval <= 600,
                "load-interval value %s is not in the range of 0 and 600", loadInterval);

        // check if it is multiple of 30
        Preconditions.checkArgument(loadInterval % 30 == 0,
                "load-interval value %s is not multiple of 30", loadInterval);

        return true;
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        if (validateConfig(dataAfter)) {
            writeCurrentAttributes(id, dataAfter, writeContext);
        } else {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        blockingDeleteAndRead(cli, id, fT(MOD_CURR_ATTR,
                "delete", true,
                "ifcName", ifcName));
    }
}
