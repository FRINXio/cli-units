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

import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.iosxr.IosXrDevices;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class LldpUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private final io.frinx.cli.unit.ios.lldp.LldpUnit delegate;
    private TranslationUnitCollector.Registration reg;

    public LldpUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
        this.delegate = new io.frinx.cli.unit.ios.lldp.LldpUnit(registry) {
            @Override
            protected String getShowHostnameCommand() {
                return "sh ru | utility egrep \"^hostname|^domain name\"";
            }
        };
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return delegate.getYangSchemas();
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return delegate.getRpcs(context);
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder writeRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder readRegistry,
                                @Nonnull Context context) {
        delegate.provideHandlers(writeRegistry, readRegistry, context);
    }

    @Override
    public String toString() {
        return "IOS XR LLDP (Openconfig) translate unit";
    }
}
