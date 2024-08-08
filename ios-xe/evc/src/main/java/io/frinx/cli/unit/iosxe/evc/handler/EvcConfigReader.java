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

package io.frinx.cli.unit.iosxe.evc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.unit.utils.CliConfigReader;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.Evc;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.evc.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.evc.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class EvcConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) {
        String evcName = instanceIdentifier.firstKeyOf(Evc.class).getName();
        parseEvcConfig(configBuilder, evcName);
    }

    @VisibleForTesting
    static void parseEvcConfig(ConfigBuilder builder, String evcName) {
        builder.setName(evcName);
    }
}