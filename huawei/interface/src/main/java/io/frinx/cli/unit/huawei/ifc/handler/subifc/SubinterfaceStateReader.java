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

package io.frinx.cli.unit.huawei.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.unit.utils.CliOperReader;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceStateReader implements CliOperReader<State, StateBuilder> {

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id, @NotNull StateBuilder builder,
                                      @NotNull ReadContext ctx) {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);
        builder.setIndex(subKey.getIndex());
    }
}