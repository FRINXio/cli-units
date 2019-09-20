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

package io.frinx.cli.unit.sros.conf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadata;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadataBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConfigMetadataReaderTest {
    private static String CONFIGURATION = "\n"
        + "\n"
        + "exit all\n"
        + "configure\n"
        + "echo \"System Configuration\"\n"
        + "    system\n"
        + "        name \"TKEuqa-01\"\n"
        + "        boot-good-exec \"cf3:\\script\\MED-All-Add100.txt\"\n"
        + "        load-balancing\n"
        + "            lsr-load-balancing lbl-ip\n"
        + "        exit\n"
        + "        management-interface\n"
        + "            yang-modules\n"
        + "                no nokia-modules\n"
        + "            exit\n"
        + "        exit\n"
        + "    exit\n"
        + "echo \"System Security Configuration\"\n"
        + "    system\n"
        + "        security\n"
        + "            telnet-server\n"
        + "            profile \"netconf\"\n"
        + "                default-action permit-all\n"
        + "                netconf\n"
        + "                    base-op-authorization\n"
        + "                        kill-session\n"
        + "                        lock\n"
        + "                    exit\n"
        + "                exit\n"
        + "            exit\n"
        + "            vprn-network-exceptions\n"
        + "        exit\n"
        + "    exit\n"
        + "\n"
        + "\n"
        + "exit all";

    private static String FINGERPRINT = "818e6d293c478e0ed284ebc74f79d9a1";

    private static String SHOW_RUNNING_CONFIG = ConfigMetadataReader.SHOW_RUNNING_CONFIG;

    @Mock
    private Cli cli;

    @Mock
    private ReadContext readContext;

    private ConfigMetadataReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ConfigMetadataReader(cli));
    }

    @Test
    public void testGetLastConfigurationFingerprint() throws Exception {
        Assert.assertThat(ConfigMetadataReader.getLastConfigurationFingerprint(CONFIGURATION),
            CoreMatchers.equalTo(FINGERPRINT));

        Assert.assertThat(ConfigMetadataReader.getLastConfigurationFingerprint(CONFIGURATION + " "),
            CoreMatchers.not(CoreMatchers.equalTo(FINGERPRINT)));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        InstanceIdentifier<ConfigurationMetadata> id = InstanceIdentifier.create(ConfigurationMetadata.class);
        ConfigurationMetadataBuilder builder = Mockito.mock(ConfigurationMetadataBuilder.class);

        Mockito.doReturn(CONFIGURATION).when(target).blockingRead(SHOW_RUNNING_CONFIG, cli, id, readContext);
        Mockito.doReturn(builder).when(builder).setLastConfigurationFingerprint(FINGERPRINT);

        target.readCurrentAttributes(id, builder, readContext);

        Mockito.verify(target).blockingRead(SHOW_RUNNING_CONFIG, cli, id, readContext);
        Mockito.verify(builder).setLastConfigurationFingerprint(FINGERPRINT);
    }
}