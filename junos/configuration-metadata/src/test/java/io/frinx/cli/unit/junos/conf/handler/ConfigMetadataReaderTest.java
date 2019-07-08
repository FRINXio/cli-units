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

package io.frinx.cli.unit.junos.conf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import java.util.Optional;
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
    private static final String EXPECTED_LAST_COMMIT = "2018-10-05 11:06:58 UTC by root";
    private static final String LAST_COMMIT_LINE_NORMAL = "## Last commit: " + EXPECTED_LAST_COMMIT;
    private static final String LAST_COMMIT_LINE_MULTI = "## Last commit: " + EXPECTED_LAST_COMMIT + "\n"
        + "## Last commit: 2018-10-10 10:10:10 UTC by root";
    private static final String LAST_COMMIT_LINE_EMPTY = "";

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
    public void testGetLastConfigurationFingerprintExpected() throws Exception {
        Optional<String> result = ConfigMetadataReader.getLastConfigurationFingerprint(LAST_COMMIT_LINE_NORMAL);

        Assert.assertThat(result.isPresent(), CoreMatchers.is(true));
        Assert.assertThat(result.get(), CoreMatchers.equalTo(EXPECTED_LAST_COMMIT));
    }

    @Test
    public void testGetLastConfigurationFingerprintMulti() throws Exception {
        Optional<String> result = ConfigMetadataReader.getLastConfigurationFingerprint(LAST_COMMIT_LINE_MULTI);

        Assert.assertThat(result.isPresent(), CoreMatchers.is(true));
        Assert.assertThat(result.get(), CoreMatchers.equalTo(EXPECTED_LAST_COMMIT));
    }

    @Test
    public void testGetLastConfigurationFingerprintEmpty() throws Exception {
        Optional<String> result = (ConfigMetadataReader.getLastConfigurationFingerprint(LAST_COMMIT_LINE_EMPTY));

        Assert.assertThat(result.isPresent(), CoreMatchers.is(false));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        InstanceIdentifier<ConfigurationMetadata> id = InstanceIdentifier.create(ConfigurationMetadata.class);
        ConfigurationMetadataBuilder builder = Mockito.mock(ConfigurationMetadataBuilder.class);

        Mockito.doReturn(LAST_COMMIT_LINE_NORMAL)
                .when(target).blockingRead(ConfigMetadataReader.SHOW_LAST_COMMIT_TIME, cli, id, readContext);
        Mockito.doReturn(builder).when(builder).setLastConfigurationFingerprint(EXPECTED_LAST_COMMIT);

        target.readCurrentAttributes(id, builder, readContext);

        Mockito.verify(target).blockingRead(ConfigMetadataReader.SHOW_LAST_COMMIT_TIME, cli, id, readContext);
        Mockito.verify(builder).setLastConfigurationFingerprint(EXPECTED_LAST_COMMIT);
    }
}