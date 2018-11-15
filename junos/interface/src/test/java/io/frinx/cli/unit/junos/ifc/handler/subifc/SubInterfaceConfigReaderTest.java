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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubInterfaceConfigReaderTest {

    @Mock
    private Cli cli;

    private SubinterfaceConfigReader target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new SubinterfaceConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).child(Config.class);

        final ConfigBuilder config1Builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String outputSingleInterface =
            "set interfaces ge-0/0/4 unit 0 description TEST_ge-0/0/4";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(id), Mockito.eq(readContext));

        target.readCurrentAttributes(id, config1Builder, readContext);

        // verify
        Assert.assertEquals(config1Builder.getDescription(), "TEST_ge-0/0/4");
    }

    @Test
    public void testMerge() {
        final SubinterfaceBuilder interfaceBuilder = new SubinterfaceBuilder();
        final Config config1 = new ConfigBuilder().build();

        target.merge(interfaceBuilder, config1);

        Assert.assertSame(interfaceBuilder.getConfig(), config1);
    }
}