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

package io.frinx.cli.unit.iosxr.lldp;

import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Command;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LldpUnit extends AbstractUnit {

    private final io.frinx.cli.unit.ios.lldp.LldpUnit delegate;

    public LldpUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
        this.delegate = new io.frinx.cli.unit.ios.lldp.LldpUnit(registry) {
            @Override
            protected Command getShowHostnameCommand() {
                return Command.showCommandNoCaching("show running-config | utility egrep \"^hostname|^domain name\"");
            }
        };
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR LLDP (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return delegate.getYangSchemas();
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder writeRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder readRegistry,
                                @Nonnull Context context) {
        delegate.provideHandlers(writeRegistry, readRegistry, context);
    }
}
