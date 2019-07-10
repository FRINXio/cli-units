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

package io.frinx.cli.unit.iosxr.isis;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.isis.handler.global.IsisGlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.iosxr.isis.handler.global.IsisGlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.isis.handler.global.IsisGlobalConfigAugWriter;
import io.frinx.cli.unit.iosxr.isis.handler.global.IsisGlobalConfigReader;
import io.frinx.cli.unit.iosxr.isis.handler.global.IsisRedistributionConfigReader;
import io.frinx.cli.unit.iosxr.isis.handler.global.IsisRedistributionConfigWriter;
import io.frinx.cli.unit.iosxr.isis.handler.global.IsisRedistributionReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXRIsisUnit extends AbstractUnit {

    public IosXRIsisUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR IS-IS unit";
    }

    @Override
    public void provideHandlers(
        @Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
        @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
        @Nonnull Context context) {

        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder rreg, Cli cli) {
        rreg.subtreeAddAfter(IIDs.NE_NE_PR_PR_IS_GL_CONFIG, new IsisGlobalConfigReader(cli),
            Sets.newHashSet(IIDs.NE_NE_PR_PR_IS_GL_CO_AUG_ISISGLOBALCONFAUG),IIDs.NE_NE_PR_PR_CONFIG);
        rreg.subtreeAddAfter(IIDs.NE_NE_PR_PR_IS_GL_AF_AF, new IsisGlobalAfiSafiReader(cli),
            Sets.newHashSet(IIDs.NE_NE_PR_PR_IS_GL_AF_AF_CONFIG), IIDs.NE_NE_PR_PR_IS_GL_CONFIG);
        rreg.add(IIDs.NE_NE_PR_PR_IS_GL_AF_AF_AUG_ISISGLOBALAFISAFICONFAUG_RE_REDISTRIBUTION,
            new IsisRedistributionReader(cli));
        rreg.add(IIDs.NE_NE_PR_PR_IS_GL_AF_AF_AUG_ISISGLOBALAFISAFICONFAUG_RE_RE_CONFIG,
            new IsisRedistributionConfigReader(cli));
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder wreg, Cli cli) {
        wreg.addNoop(IIDs.NE_NE_PR_PR_ISIS);
        wreg.addNoop(IIDs.NE_NE_PR_PR_IS_GLOBAL);
        wreg.addNoop(IIDs.NE_NE_PR_PR_IS_GL_CONFIG);
        wreg.addAfter(IIDs.NE_NE_PR_PR_IS_GL_CO_AUG_ISISGLOBALCONFAUG, new IsisGlobalConfigAugWriter(cli),
            IIDs.NE_NE_PR_PR_CONFIG);
        wreg.addNoop(IIDs.NE_NE_PR_PR_IS_GL_AFISAFI);
        wreg.addNoop(IIDs.NE_NE_PR_PR_IS_GL_AF_AF);
        wreg.addAfter(IIDs.NE_NE_PR_PR_IS_GL_AF_AF_CONFIG, new IsisGlobalAfiSafiConfigWriter(cli),
            IIDs.NE_NE_PR_PR_IS_GL_CONFIG);

        wreg.addNoop(IIDs.NE_NE_PR_PR_IS_GL_AF_AF_AUG_ISISGLOBALAFISAFICONFAUG);
        wreg.addNoop(IIDs.NE_NE_PR_PR_IS_GL_AF_AF_AUG_ISISGLOBALAFISAFICONFAUG_REDISTRIBUTIONS);
        wreg.addNoop(IIDs.NE_NE_PR_PR_IS_GL_AF_AF_AUG_ISISGLOBALAFISAFICONFAUG_RE_REDISTRIBUTION);
        wreg.addAfter(IIDs.NE_NE_PR_PR_IS_GL_AF_AF_AUG_ISISGLOBALAFISAFICONFAUG_RE_RE_CONFIG,
            new IsisRedistributionConfigWriter(cli), IIDs.NE_NE_PR_PR_IS_GL_AF_AF_CONFIG);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
            io.frinx.openconfig.openconfig.isis.IIDs.FRINX_OPENCONFIG_ISIS,
            io.frinx.openconfig.openconfig.isis.IIDs.FRINX_OPENCONFIG_ISIS_TYPES,
            IIDs.FRINX_ISIS_EXTENSION);
    }
}
