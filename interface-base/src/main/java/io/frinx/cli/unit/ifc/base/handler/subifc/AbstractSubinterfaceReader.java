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

package io.frinx.cli.unit.ifc.base.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractSubinterfaceReader implements CliConfigListReader<Subinterface, SubinterfaceKey,
        SubinterfaceBuilder> {

    public static final long ZERO_SUBINTERFACE_ID = 0L;

    private Cli cli;

    protected AbstractSubinterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<SubinterfaceKey> getAllIds(@NotNull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @NotNull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        return parseSubinterfaceIds(blockingRead(getReadCommand(), cli, instanceIdentifier, readContext), ifcName);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Subinterface> id,
                                      @NotNull SubinterfaceBuilder builder,
                                      @NotNull ReadContext readContext) {
        builder.setIndex(id.firstKeyOf(Subinterface.class).getIndex());
    }

    protected abstract String getReadCommand();

    protected abstract List<SubinterfaceKey> parseSubinterfaceIds(String output, String ifcName);

    public static boolean isSubInterfaceZero(InstanceIdentifier<?> instanceIdentifier) {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();
        return subId == ZERO_SUBINTERFACE_ID;
    }
}