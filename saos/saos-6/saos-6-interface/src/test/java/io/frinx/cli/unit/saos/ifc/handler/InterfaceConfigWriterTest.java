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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.IngressToEgressQmap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.VlanEthertypePolicy;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriterTest {

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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        dataBefore = createConfig(true, EthernetCsmacd.class, 35, "test",
                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                        VlanEthertypePolicy.VlanTpid,  false));

        lagDataBefore = createConfig(true, Ieee8023adLag.class, 35, "lag interface", null);
    }

    @Test
    public void write() {
        try {
            this.writer.writeCurrentAttributes(iid, dataBefore, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            TestCase.fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test(expected = WriteFailedException.CreateFailedException.class)
    public void writeLag() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, lagDataBefore, context);
    }

    @Test
    public void updateTemplateTest() throws WriteFailedException {
        // nothing
        Assert.assertEquals("",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false)))
        );

        // enabled
        Assert.assertEquals("port disable port test_name\n",
                writer.updateTemplate(dataBefore,
                        createConfig(false, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false))));

        // mtu
        Assert.assertEquals("port set port test_name max-frame-size 1500\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 1500, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false))));

        // description
        Assert.assertEquals("port set port test_name description \"new description\"\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "new description",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false))));

        // physical type
        Assert.assertEquals("port set port test_name mode sfp\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Sfp, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  false))));
        // ingress to egress qmap
        Assert.assertEquals("port set port test_name ingress-to-egress-qmap NNI-NNI\n",
                writer.updateTemplate(dataBefore,
                       createConfig(true, EthernetCsmacd.class, 35, "test",
                               createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.NNINNI, 200, true,
                                       VlanEthertypePolicy.VlanTpid,  false))));

        // max dynamic
        Assert.assertEquals("flow access-control set port test_name max-dynamic-macs 300\n",
                writer.updateTemplate(dataBefore,
                       createConfig(true, EthernetCsmacd.class, 35, "test",
                               createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 300, true,
                                       VlanEthertypePolicy.VlanTpid,  false))));

        // ingress filter
        Assert.assertEquals("port set port test_name vs-ingress-filter off\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, false,
                                        VlanEthertypePolicy.VlanTpid,  false))));

        // ethertype policy
        Assert.assertEquals("virtual-circuit ethernet set port test_name vlan-ethertype-policy all\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.All,  false))));

        // forward unlearned
        Assert.assertEquals("flow access-control set port test_name forward-unlearned on\n",
                writer.updateTemplate(dataBefore,
                        createConfig(true, EthernetCsmacd.class, 35, "test",
                                createAugBuilder(PhysicalType.Rj45, IngressToEgressQmap.DefaultRCOS, 200, true,
                                        VlanEthertypePolicy.VlanTpid,  true))));

        // all
        Assert.assertEquals("port disable port test_name\n"
                + "port set port test_name description \"new description\"\n"
                + "port set port test_name max-frame-size 1500\n"
                + "port set port test_name mode sfp\n"
                + "port set port test_name vs-ingress-filter off\n"
                + "virtual-circuit ethernet set port test_name vlan-ethertype-policy all\n"
                + "port set port test_name ingress-to-egress-qmap NNI-NNI\n"
                + "flow access-control set port test_name max-dynamic-macs 350\n"
                + "flow access-control set port test_name forward-unlearned on\n",
                writer.updateTemplate(dataBefore,
                        createConfig(false, EthernetCsmacd.class, 1500, "new description",
                                createAugBuilder(PhysicalType.Sfp, IngressToEgressQmap.NNINNI, 350, false,
                                        VlanEthertypePolicy.All,  true))));
    }

    @Test
    public void delete() {
        try {
            this.writer.deleteCurrentAttributes(iid, dataBefore, context);
            Mockito.verify(cli).executeAndRead(response.capture());
            TestCase.fail();
        } catch (WriteFailedException e) {
            // ok
        }
    }

    @Test(expected = WriteFailedException.DeleteFailedException.class)
    public void deleteLag() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, lagDataBefore, context);
    }

    private IfSaosAugBuilder createAugBuilder(PhysicalType type, IngressToEgressQmap qmap, Integer maxDynamic,
                                              Boolean ingressFilter, VlanEthertypePolicy etherTypePolicy,
                                              Boolean fwUnlearned) {
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

        return builder;
    }

    private Config createConfig(@Nonnull Boolean enabled, @Nonnull Class<? extends InterfaceType> type,
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