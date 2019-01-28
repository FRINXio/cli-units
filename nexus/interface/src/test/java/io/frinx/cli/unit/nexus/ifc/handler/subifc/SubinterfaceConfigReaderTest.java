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

package io.frinx.cli.unit.nexus.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigReaderTest {

    private static final String SH_RUN_INT = "Fri Nov 23 13:18:34.834 UTC\n"
            + "interface Ethernet1/1.5\n"
            + " description example\n"
            + " no shutdown\n"
            + "\n";

    private SubinterfaceConfigReader target;

    @Mock
    private Cli cli;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new SubinterfaceConfigReader(cli));
    }

    private static final Config EXPECTED_CONFIG = new ConfigBuilder().setName("Ethernet1/1.5")
            .setEnabled(true)
            .setIndex(5L)
            .setDescription("example")
            .build();

    @Test
    public void testParseInterface() throws Exception {
        final String interfaceName = "Ethernet1/1";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(5));
        final ReadContext readContext = Mockito.mock(ReadContext.class);


        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey)
                .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).child(Config.class);

        final ConfigBuilder actualConfig = new ConfigBuilder();

        Mockito.doReturn(SH_RUN_INT).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(id), Mockito.eq(readContext));


        target.readCurrentAttributes(id, actualConfig, readContext);
        Assert.assertEquals(EXPECTED_CONFIG, actualConfig.build());

    }
}