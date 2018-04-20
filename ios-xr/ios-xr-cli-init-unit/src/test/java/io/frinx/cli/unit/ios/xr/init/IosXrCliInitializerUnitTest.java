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

package io.frinx.cli.unit.ios.xr.init;

import io.fd.honeycomb.translate.spi.write.CommitFailedException;
import io.fd.honeycomb.translate.write.registry.WriterRegistry;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;

public class IosXrCliInitializerUnitTest {

    private static final String SUCCESS_COMMIT = "Mon Feb 12 19:29:16.935 UTC\n" +
            "RP/0/0/CPU0:XR-5(config)#";

    private static final String FAILED_COMMIT = "Mon Feb 12 19:29:14.715 UTC\n" +
            "\n" +
            "% Failed to commit one or more configuration items during a pseudo-atomic operation. All changes made have been reverted. Please issue 'show configuration failed [inheritance]' from this session to view the errors\n" +
            "RP/0/0/CPU0:XR-5(config)#\n";

    private IosXrCliInitializerUnit unit;

    @Mock
    private TranslationUnitCollector registry;

    @Mock
    private TranslateUnit.Context context;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.unit = new IosXrCliInitializerUnit(this.registry);
    }

    private CompletableFuture prepareCommit() throws Exception {
        Cli cli = Mockito.mock(Cli.class);
        Mockito.when(this.context.getTransport()).thenReturn(cli);


        CompletionStage future = Mockito.mock(CompletionStage.class);
        CompletableFuture cFuture = Mockito.mock(CompletableFuture.class);
        Mockito.when(future.toCompletableFuture()).thenReturn(cFuture);
        Mockito.when(cli.executeAndRead(Mockito.anyString())).thenReturn(future);
        Mockito.when(cli.executeAndSwitchPrompt(Mockito.anyString(), Mockito.any(Predicate.class))).thenReturn(future);
        RemoteDeviceId device = new RemoteDeviceId(new TopologyKey(new TopologyId("cli")),
                "deviceId",
                new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 1234));
        unit.getInitializer(device, new CliNodeBuilder().build());
        return cFuture;
    }

    @Test
    public void testSuccessCommit() throws Exception {
        CompletableFuture cFuture = prepareCommit();
        Mockito.when(cFuture.get()).thenReturn(SUCCESS_COMMIT);
        try {
            this.unit.getCommitHook(this.context).run();
            Assert.assertTrue(true);
        } catch (CommitFailedException e) {
            Assert.fail("Success commit shouldn't emit exception.");
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFailedCommit() throws Exception {
        CompletableFuture cFuture = prepareCommit();
        Mockito.when(cFuture.get()).thenReturn(FAILED_COMMIT);
        thrown.expect(CommitFailedException.class);
        this.unit.getCommitHook(this.context).run();
    }

    @Test
    public void testRevertSuccessCommitFailed() throws Exception {
        CompletableFuture cFuture = prepareCommit();
        // doesn't matter what we return
        Mockito.when(cFuture.get()).thenReturn("");
        thrown.expect(WriterRegistry.Reverter.RevertSuccessException.class);
        this.unit.getPostFailedHook(this.context).run();
    }

    @Test
    public void testRevertFailedCommitFailed() throws Exception {
        CompletableFuture cFuture = prepareCommit();
        Mockito.when(cFuture.get()).thenThrow(InterruptedException.class);
        thrown.expect(WriterRegistry.Reverter.RevertFailedException.class);
        this.unit.getPostFailedHook(this.context).run();
    }
}
