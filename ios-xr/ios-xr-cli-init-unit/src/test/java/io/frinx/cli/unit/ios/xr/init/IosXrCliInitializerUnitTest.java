/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.xr.init;

import io.fd.honeycomb.translate.spi.write.CommitFailedException;
import io.fd.honeycomb.translate.write.registry.WriterRegistry;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

    private CompletableFuture prepareCommit() {
        Cli cli = Mockito.mock(Cli.class);
        Mockito.when(this.context.getTransport()).thenReturn(cli);


        CompletionStage future = Mockito.mock(CompletionStage.class);
        CompletableFuture cFuture = Mockito.mock(CompletableFuture.class);
        Mockito.when(cli.executeAndRead(Mockito.anyString())).thenReturn(future);
        Mockito.when(future.toCompletableFuture()).thenReturn(cFuture);
        return cFuture;
    }

    @Test
    public void testSuccessCommit() throws ExecutionException, InterruptedException {
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
    public void testFailedCommit() throws ExecutionException, InterruptedException, CommitFailedException {
        CompletableFuture cFuture = prepareCommit();
        Mockito.when(cFuture.get()).thenReturn(FAILED_COMMIT);
        thrown.expect(CommitFailedException.class);
        this.unit.getCommitHook(this.context).run();
    }

    @Test
    public void testRevertSuccessCommitFailed() throws ExecutionException, InterruptedException, WriterRegistry.Reverter.RevertSuccessException, WriterRegistry.Reverter.RevertFailedException {
        CompletableFuture cFuture = prepareCommit();
        // doesn't matter what we return
        Mockito.when(cFuture.get()).thenReturn("");
        thrown.expect(WriterRegistry.Reverter.RevertSuccessException.class);
        this.unit.getPostFailedHook(this.context).run();
    }

    @Test
    public void testRevertFailedCommitFailed() throws ExecutionException, InterruptedException, WriterRegistry.Reverter.RevertSuccessException, WriterRegistry.Reverter.RevertFailedException {
        CompletableFuture cFuture = prepareCommit();
        Mockito.when(cFuture.get()).thenThrow(InterruptedException.class);
        thrown.expect(WriterRegistry.Reverter.RevertFailedException.class);
        this.unit.getPostFailedHook(this.context).run();
    }
}
