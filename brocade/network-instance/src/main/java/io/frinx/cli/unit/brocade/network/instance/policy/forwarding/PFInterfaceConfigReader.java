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

package io.frinx.cli.unit.brocade.network.instance.policy.forwarding;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.vrf.policy.forwarding.VrfPFInterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;

public class PFInterfaceConfigReader extends CompositeReader<Config, ConfigBuilder>
        implements CliConfigReader<Config, ConfigBuilder> {

    public PFInterfaceConfigReader(Cli cli) {
        super(Lists.newArrayList(
                new VrfPFInterfaceConfigReader(cli)
        ));
    }
}
