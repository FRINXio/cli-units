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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.protocol;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.ReaderCustomizer;
import io.frinx.cli.handlers.network.instance.L3VrfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.ospf.handler.OspfProtocolConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class ProtocolConfigReader implements L3VrfReader.L3VrfConfigReader<Config, ConfigBuilder> {

    private final ProtocolConfigReaderComposite delegate;

    public ProtocolConfigReader(Cli cli) {
        // Wrapping the composite reader into a typed reader to ensure network instance type first
        delegate = new ProtocolConfigReaderComposite(cli);
    }

    @Override
    public void readCurrentAttributesForType(
        @Nonnull InstanceIdentifier<Config> instanceIdentifier,
        @Nonnull ConfigBuilder configBuilder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        delegate.readCurrentAttributes(instanceIdentifier, configBuilder, readContext);
    }

    public static class ProtocolConfigReaderComposite extends CompositeReader<Config, ConfigBuilder>
            implements CliConfigReader<Config, ConfigBuilder> {

        public ProtocolConfigReaderComposite(Cli cli) {
            super(new ArrayList<ReaderCustomizer<Config, ConfigBuilder>>() {{
                    add(new OspfProtocolConfigReader(cli));
                }
            });
        }

        @Override
        public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
            ((ProtocolBuilder) builder).setConfig(config);
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        delegate.merge(builder,  config);
    }

}
