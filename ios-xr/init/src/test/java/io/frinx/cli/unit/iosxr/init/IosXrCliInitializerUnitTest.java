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

package io.frinx.cli.unit.iosxr.init;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.write.CommitFailedException;
import io.fd.honeycomb.translate.write.RevertFailedException;
import io.fd.honeycomb.translate.write.RevertSuccessException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.io.impl.cli.ErrorAwareCli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IosXrCliInitializerUnitTest {

    private static final String SUCCESS_COMMIT = "Mon Feb 12 19:29:16.935 UTC\n"
            + "RP/0/0/CPU0:XR-5(config)#";

    private static final String FAILED_COMMIT = """
            Mon Feb 12 19:29:14.715 UTC

            % Failed to commit one or more configuration items during a pseudo-atomic operation. \
            All changes made have been reverted. Please issue 'show configuration failed [inheritance]' \
            from this session to view the errors
            RP/0/0/CPU0:XR-5(config)#
            """;

    private static final Set<Pattern> COMMIT_ERROR_PATTERNS = Sets.newLinkedHashSet(Collections.singletonList(
            Pattern.compile("% (?i)Failed(?-i).*", Pattern.DOTALL)
    ));

    private IosXrCliInitializerUnit unit;

    @Mock
    private Cli delegateCli;

    @Mock
    private TranslationUnitCollector registry;

    @Mock
    private TranslateUnit.Context context;

    @BeforeEach
    void setUp() throws Exception {
        this.unit = new IosXrCliInitializerUnit(this.registry);

        final String idName = "test";
        delegateCli = Mockito.mock(Cli.class);
        final Cli.Configuration delegateCliConfig = Mockito.mock(Cli.Configuration.class);
        Mockito.when(delegateCliConfig.init())
                .thenAnswer(invocation -> CompletableFuture.completedFuture(delegateCli));

        final Set<Pattern> errorPatterns = Sets.newLinkedHashSet(Collections.singletonList(
                Pattern.compile("% (?i)invalid input(?-i).*", Pattern.DOTALL)
        ));

        final CompletionStage<? extends Cli> cliStage =
                new ErrorAwareCli.Configuration(idName, delegateCliConfig, errorPatterns, Collections.emptySet(),
                        ForkJoinPool.commonPool(), 3L, 5).init();
        Cli globalCli = cliStage.toCompletableFuture()
                .get();
        Mockito.when(globalCli.executeAndSwitchPrompt(Mockito.any(Command.class), Mockito.any(Predicate.class)))
                .thenReturn(cliStage.toCompletableFuture());

        Mockito.when(this.context.getTransport())
                .thenReturn(globalCli);
        RemoteDeviceId device = new RemoteDeviceId(new TopologyKey(new TopologyId("cli")),
                "deviceId",
                new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 1234));
        unit.getInitializer(device, new CliNodeBuilder().build());
    }

    @Test
    void testSuccessCommit() {

        Mockito.when(delegateCli.executeAndRead(Mockito.any(Command.class)))
                .thenReturn(CompletableFuture.completedFuture(SUCCESS_COMMIT));
        try {
            this.unit.getCommitHook(this.context, COMMIT_ERROR_PATTERNS)
                    .run();
            assertTrue(true);
        } catch (CommitFailedException e) {
            fail("Success commit shouldn't emit exception.");
        }
    }

    @Test
    void testFailedCommit() throws Exception {
        assertThrows(CommitFailedException.class, () -> {
            Mockito.when(delegateCli.executeAndRead(Mockito.any(Command.class)))
                    .thenReturn(CompletableFuture.completedFuture(FAILED_COMMIT));
            Mockito.when(delegateCli.executeAndRead(IosXrCliInitializerUnit.SH_CONF_FAILED))
                    .thenReturn(CompletableFuture.completedFuture(""));
            this.unit.getCommitHook(this.context, COMMIT_ERROR_PATTERNS)
                    .run();
        });
    }

    @Test
    void testRevertSuccessCommitFailed() throws Exception {
        assertThrows(RevertSuccessException.class, () -> {
            // doesn't matter what we return
            Mockito.when(delegateCli.executeAndSwitchPrompt(Mockito.any(Command.class), Mockito.any(Predicate.class)))
                    .thenReturn(CompletableFuture.completedFuture(""));
            this.unit.getPostFailedHook(this.context)
                    .run(null);
        });
    }

    @Test
    @Disabled
    void testRevertFailedCommitFailed() throws Exception {
        assertThrows(RevertFailedException.class, () -> {
            Mockito.when(delegateCli.executeAndSwitchPrompt(Mockito.any(Command.class), Mockito.any(Predicate.class)))
                    .thenThrow(InterruptedException.class);
            this.unit.getPostFailedHook(this.context)
                    .run(null);
        });
    }
}