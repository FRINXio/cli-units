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

package io.frinx.cli.unit.saos.qos.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.qos.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.top.qos.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.top.qos.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class QosConfigWriterTest {

    private static final String ENABLE_COMMAND = "traffic-profiling enable\n";
    private static final String DISABLE_COMMAND = "traffic-profiling disable\n";

    @Mock
    private Cli cli;

    private QosConfigWriter writer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new QosConfigWriter(this.cli);
    }

    @Test
    public void writeTemplateTest() {
        createWriteCommandAndTest(createConfig(true, true), ENABLE_COMMAND);
        createWriteCommandAndTest(createConfig(true, false), null);
        createWriteCommandAndTest(createConfig(false, null), null);
    }

    private void createWriteCommandAndTest(Config data, String expected) {
        String command = QosConfigWriter.writeTemplate(data);
        Assert.assertEquals(expected, command);
    }

    @Test
    public void updateTemplateTest() {
        createUpdateComandAndTest(createConfig(true, false),
            createConfig(true, true), ENABLE_COMMAND);
        createUpdateComandAndTest(createConfig(true, true),
            createConfig(true, false), DISABLE_COMMAND);
        createUpdateComandAndTest(createConfig(false, null),
            createConfig(true, true), ENABLE_COMMAND);
        createUpdateComandAndTest(createConfig(false, null),
            createConfig(true, false), DISABLE_COMMAND);
        createUpdateComandAndTest(createConfig(false, null),
            createConfig(false, null), null);
    }

    private void createUpdateComandAndTest(Config before, Config after, String expected) {
        String command = writer.updateTemplate(before, after);
        Assert.assertEquals(expected, command);
    }

    @Test
    public void deleteCurrentAttributesTest() throws WriteFailedException {
        InstanceIdentifier id = IIDs.QO_CONFIG;
        Config data = createConfig(true, true);
        WriteContext context = Mockito.mock(WriteContext.class);
        ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

        writer.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DISABLE_COMMAND, response.getValue().getContent());
    }

    private Config createConfig(boolean addAugmentation, Boolean value) {
        if (addAugmentation) {
            return new ConfigBuilder()
                .addAugmentation(SaosQosAug.class, new SaosQosAugBuilder().setEnabled(value).build()).build();
        }
        return new ConfigBuilder().build();
    }
}
