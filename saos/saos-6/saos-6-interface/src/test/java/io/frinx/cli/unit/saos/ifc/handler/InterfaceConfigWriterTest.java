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

package io.frinx.cli.unit.saos.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.IngressToEgressQmap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.VlanEthertypePolicy;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    // test data
    private Config dataBefore;
    private Config lagDataBefore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        dataBefore = createConfig(true, EthernetCsmacd.class, 35, "test",
                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null));

        lagDataBefore = createConfig(true, Ieee8023adLag.class, 35, "lag interface", null);
    }

    @Test
    void write() {
        try {
            this.writer.writeCurrentAttributes(iid, dataBefore, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test
    void writeLag() throws WriteFailedException {
        assertThrows(WriteFailedException.CreateFailedException.class, () -> {
            writer.writeCurrentAttributes(iid, lagDataBefore, context);
        });
    }

    @Test
    void updateTemplateTest() throws WriteFailedException {
        // nothing
        assertEquals("",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null)))
        );

        // enabled
        assertEquals("port disable port test_name\n",
                writer.updateTemplate(dataBefore,
                        createConfig(false, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null))));

        // mtu
        assertEquals("port set port test_name max-frame-size 1500\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 1500, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null))));

        // description
        assertEquals("port set port test_name description \"new description\"\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "new description",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null))));

        // physical type
        assertEquals("port set port test_name mode sfp\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Sfp, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null))));
        // ingress to egress qmap
        assertEquals("port set port test_name ingress-to-egress-qmap NNI-NNI\n",
                writer.updateTemplate(dataBefore,
                       createConfig(true, EthernetCsmacd.class, 35, "test",
                               createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.NNINNI, 200, true,
                                       VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null))));

        // max dynamic
        assertEquals("flow access-control set port test_name max-dynamic-macs 300\n",
                writer.updateTemplate(dataBefore,
                       createConfig(true, EthernetCsmacd.class, 35, "test",
                               createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 300, true,
                                       VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null))));

        // ingress filter
        assertEquals("port set port test_name vs-ingress-filter off\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, false,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, false, null))));

        // resolved-cos-remark-l2
        assertEquals("port set port test_name resolved-cos-remark-l2 true\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, true, false, null))));

        // ethertype policy
        assertEquals("virtual-circuit ethernet set port test_name vlan-ethertype-policy all\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.All,  false, true, true, false, false, null))));

        // forward unlearned
        assertEquals("flow access-control set port test_name forward-unlearned on\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  true, true, true, false, false, null))));

        // rstp disabled
        assertEquals("rstp disable port test_name\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, false, true, false, false, null))));

        // mstp disabled
        assertEquals("mstp disable port test_name\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, false, false, false, null))));

        // speed
        assertEquals("port set port test_name speed gigabit\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, false,
                                        SaosIfExtensionConfig.SpeedType.Gigabit))));

        // negotiation auto
        assertEquals("port set port test_name auto-neg on\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false, true, true, false, true, null))));

        // all
        assertEquals("""
                        port disable port test_name
                        port set port test_name description "new description"
                        port set port test_name max-frame-size 1500
                        port set port test_name mode sfp
                        port set port test_name vs-ingress-filter off
                        port set port test_name resolved-cos-remark-l2 true
                        virtual-circuit ethernet set port test_name vlan-ethertype-policy all
                        port set port test_name ingress-to-egress-qmap NNI-NNI
                        flow access-control set port test_name max-dynamic-macs 350
                        flow access-control set port test_name forward-unlearned on
                        rstp disable port test_name
                        mstp disable port test_name
                        port set port test_name speed gigabit
                        port set port test_name auto-neg on
                        """,
                writer.updateTemplate(dataBefore,
                        createConfig(false, EthernetCsmacd.class, 1500, "new description",
                                createAugBuilder(PhysicalType.Sfp, IngressToEgressQmap.NNINNI, 350, false,
                                        VlanEthertypePolicy.All,  true, false, false,
                                        true, true, SaosIfExtensionConfig.SpeedType.Gigabit))));
    }

    @Test
    void delete() {
        try {
            this.writer.deleteCurrentAttributes(iid, dataBefore, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test
    void deleteLag() throws WriteFailedException {
        assertThrows(WriteFailedException.DeleteFailedException.class, () -> {
            writer.deleteCurrentAttributes(iid, lagDataBefore, context);
        });
    }

    private IfSaosAugBuilder createAugBuilder(PhysicalType type, IngressToEgressQmap qmap, Integer maxDynamic,
                                              Boolean ingressFilter, VlanEthertypePolicy etherTypePolicy,
                                              Boolean fwUnlearned, Boolean rstpEnabled, Boolean mstpEnabled,
                                              Boolean resolvedCosRemarkL2, Boolean negotiationAuto,
                                              SaosIfExtensionConfig.SpeedType speedType) {
        IfSaosAugBuilder builder = new IfSaosAugBuilder();

        if (type != null) {
            builder.setPhysicalType(type);
        }
        if (qmap != null) {
            builder.setIngressToEgressQmap(qmap);
        }
        if (maxDynamic != null) {
            builder.setMaxDynamicMacs(maxDynamic);
        }
        if (ingressFilter != null) {
            builder.setVsIngressFilter(ingressFilter);
        }
        if (etherTypePolicy != null) {
            builder.setVlanEthertypePolicy(etherTypePolicy);
        }
        if (fwUnlearned != null) {
            builder.setForwardUnlearned(fwUnlearned);
        }
        if (rstpEnabled != null) {
            builder.setRstpEnabled(rstpEnabled);
        }
        if (mstpEnabled != null) {
            builder.setMstpEnabled(mstpEnabled);
        }
        if (resolvedCosRemarkL2 != null) {
            builder.setResolvedCosRemarkL2(resolvedCosRemarkL2);
        }
        if (negotiationAuto != null) {
            builder.setNegotiationAuto(negotiationAuto);
        }
        if (speedType != null) {
            builder.setSpeedType(speedType);
        }

        return builder;
    }

    private Config createConfig(@NotNull Boolean enabled, @NotNull Class<? extends InterfaceType> type,
                                Integer mtu, String desc, IfSaosAugBuilder augBuilder) {
        ConfigBuilder builder = new ConfigBuilder().setName("test_name").setEnabled(enabled).setType(type);

        if (mtu != null) {
            builder.setMtu(mtu);
        }
        if (desc != null) {
            builder.setDescription(desc);
        }

        return augBuilder != null
                ? builder.addAugmentation(IfSaosAug.class, augBuilder.build()).build() : builder.build();
    }
}