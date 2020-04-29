/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.qos;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos.qos.handler.QosConfigReader;
import io.frinx.cli.unit.saos.qos.handler.QosConfigWriter;
import io.frinx.cli.unit.saos.qos.handler.ifc.InterfaceConfigReader;
import io.frinx.cli.unit.saos.qos.handler.ifc.InterfaceConfigWriter;
import io.frinx.cli.unit.saos.qos.handler.ifc.InterfaceReader;
import io.frinx.cli.unit.saos.qos.handler.scheduler.SchedulerConfigReader;
import io.frinx.cli.unit.saos.qos.handler.scheduler.SchedulerPolicyConfigReader;
import io.frinx.cli.unit.saos.qos.handler.scheduler.SchedulerPolicyReader;
import io.frinx.cli.unit.saos.qos.handler.scheduler.SchedulerPolicyWriter;
import io.frinx.cli.unit.saos.qos.handler.scheduler.SchedulerReader;
import io.frinx.cli.unit.saos.qos.handler.scheduler.TwoRateThreeColorConfigReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.qos.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosQosUnit extends AbstractUnit {

    public SaosQosUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return SaosDevices.SAOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Saos-6 Qos (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_QOS,
                IIDs.FRINX_SAOS_QOS_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();

        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.QOS);
        writeRegistry.subtreeAdd(IIDs.QO_CONFIG, new QosConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_CO_AUG_SAOSQOSAUG));
        writeRegistry.addNoop(IIDs.QO_IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.QO_IN_IN_CONFIG, new InterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_CO_AUG_SAOSQOSIFAUG));
        writeRegistry.subtreeAdd(IIDs.QO_SC_SCHEDULERPOLICY, new SchedulerPolicyWriter(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_CONFIG,
                        IIDs.QO_SC_SC_CO_AUG_SAOSQOSSCPOLICYIFCID,
                        IIDs.QO_SC_SC_SCHEDULERS,
                        IIDs.QO_SC_SC_SC_SCHEDULER,
                        IIDs.QO_SC_SC_SC_SC_CONFIG,
                        IIDs.QO_SC_SC_SC_SC_CO_AUG_SAOSQOSSCHEDULERAUG,
                        IIDs.QO_SC_SC_SC_SC_TWORATETHREECOLOR,
                        IIDs.QO_SC_SC_SC_SC_TW_CONFIG,
                        IIDs.QO_SC_SC_SC_SC_TW_CO_AUG_SAOSQOS2R3CAUG));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.QO_CONFIG, new QosConfigReader(cli));
        readRegistry.add(IIDs.QO_IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.QO_IN_IN_CONFIG, new InterfaceConfigReader(cli));
        readRegistry.add(IIDs.QO_SC_SCHEDULERPOLICY, new SchedulerPolicyReader(cli));
        readRegistry.add(IIDs.QO_SC_SC_CONFIG, new SchedulerPolicyConfigReader(cli));
        readRegistry.add(IIDs.QO_SC_SC_SC_SCHEDULER, new SchedulerReader(cli));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_CONFIG, new SchedulerConfigReader(cli));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_TW_CONFIG, new TwoRateThreeColorConfigReader(cli));
    }
}
