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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpinterval;

import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Disabled
@ExtendWith(MockitoExtension.class)
class BundleEtherLacpIntervalConfigReaderTest {
    private static final List<String> GET_PHYSICAL_PORTS_RESULT =
            Lists.newArrayList("1/1", "1/2", "3/1", "3/2", "3/3", "3/4", "7/1", "7/2", "t/1", "t/2");

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private BundleEtherLacpIntervalConfigReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new BundleEtherLacpIntervalConfigReader(cli));
    }

    @Test
    void testParseEthernetConfig() throws Exception {
        final String output = "lacp port timeout 3/4,7/1-7/2 short";
        final String id = "3/4";
        List<String> portList = new ArrayList<>();
        portList.add("3/4");
        portList.add("7/1");
        portList.add("7/2");

        ConfigBuilder builder = new ConfigBuilder();
        // test
        BundleEtherLacpIntervalConfigReader.parseEthernetConfig(output, builder, portList, id);

        assertThat(builder.getAugmentation(LacpEthConfigAug.class).getInterval(),
                CoreMatchers.is(LacpPeriodType.FAST));
    }


    @Test
    void testReadCurrentAttributes() throws Exception {
        final String interfaceName = "Ethernet3/4";
        final InstanceIdentifier<Config> id = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(interfaceName))
                .augmentation(Interface1.class)
                .child(Ethernet.class)
                .child(Config.class);
        final String output = "lacp port timeout 3/4,7/1-7/2 short";
        final ConfigBuilder builder = new ConfigBuilder();

        Mockito.doReturn(output).when(target)
                .blockingRead(BundleEtherLacpIntervalConfigReader.SHOW_LACP_PORT, cli, id, readContext);

        try (MockedStatic<DasanCliUtil> mock = Mockito.mockStatic(DasanCliUtil.class)) {
            mock.when(() -> DasanCliUtil
                    .getPhysicalPorts(cli, target, id, readContext)).thenReturn(GET_PHYSICAL_PORTS_RESULT);
            mock.when(() -> DasanCliUtil
                            .containsPort(GET_PHYSICAL_PORTS_RESULT, "3/4,7/1-7/2", "3/4"))
                    .thenReturn(Boolean.TRUE);

            // test
            target.readCurrentAttributes(id, builder, readContext);

            assertThat(builder.getAugmentation(LacpEthConfigAug.class).getInterval(),
                    CoreMatchers.is(LacpPeriodType.FAST));
        }
    }
}