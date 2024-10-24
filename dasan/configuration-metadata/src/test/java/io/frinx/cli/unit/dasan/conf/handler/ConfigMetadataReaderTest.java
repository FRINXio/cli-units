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

package io.frinx.cli.unit.dasan.conf.handler;

import static org.hamcrest.MatcherAssert.assertThat;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadata;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadataBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class ConfigMetadataReaderTest {
    private static String CONFIGURATION = StringUtils.join(new String[] {
        "bridge",
        "  vlan create 1129,1133,1137,1141,1145,1149,1153,1157 eline",
        "  vlan add br10 3/3 tagged",
        "  lacp aggregator 8",
        "  lacp port 3/4,4/4 aggregator 8",
        "  jumbo-frame 3/3,4/3 2018",
        "  port disable 3/3",
        "  lacp port admin-key 7/1,8/1 2" ,
        "  vlan add 1455 t/21 tagged",
        "",
        "interface br10",
        "  no shutdown",
        "  no ip redirects",
        "  mtu 2000",
        "  ip address 10.187.100.49/28",
    }, "\n");

    private static String FINGERPRINT = "4548328c2bd802a856ddd902cfe86198";

    private static String SHOW_RUNNING_CONFIG = ConfigMetadataReader.SHOW_RUNNING_CONFIG;

    @Mock
    private Cli cli;

    @Mock
    private ReadContext readContext;

    private ConfigMetadataReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ConfigMetadataReader(cli));
    }

    @Test
    void testGetLastConfigurationFingerprint() throws Exception {
        assertThat(ConfigMetadataReader.getLastConfigurationFingerprint(CONFIGURATION),
            CoreMatchers.equalTo(FINGERPRINT));

        assertThat(ConfigMetadataReader.getLastConfigurationFingerprint(CONFIGURATION + " "),
            CoreMatchers.not(CoreMatchers.equalTo(FINGERPRINT)));
    }

    @Test
    void testReadCurrentAttributes() throws Exception {
        InstanceIdentifier<ConfigurationMetadata> id = InstanceIdentifier.create(ConfigurationMetadata.class);
        ConfigurationMetadataBuilder builder = Mockito.mock(ConfigurationMetadataBuilder.class);

        Mockito.doReturn(CONFIGURATION).when(target).blockingRead(SHOW_RUNNING_CONFIG, cli, id, readContext);
        Mockito.doReturn(builder).when(builder).setLastConfigurationFingerprint(FINGERPRINT);

        target.readCurrentAttributes(id, builder, readContext);

        Mockito.verify(target).blockingRead(SHOW_RUNNING_CONFIG, cli, id, readContext);
        Mockito.verify(builder).setLastConfigurationFingerprint(FINGERPRINT);
    }
}