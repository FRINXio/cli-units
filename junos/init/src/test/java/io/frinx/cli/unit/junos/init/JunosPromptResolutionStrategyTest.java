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

package io.frinx.cli.unit.junos.init;

import io.frinx.cli.io.PromptResolutionStrategy;
import io.frinx.cli.io.Session;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JunosPromptResolutionStrategyTest {
    private static final String EXPECTED_PROMPT = "root@VS999#";

    private static final String OUTPUT_EMPTY = "";
    private static final String OUTPUT_UNEXPECTED = "root@VS002#\n"
        + "value1\n"
        + "root@VS002#";
    private static final String OUTPUT_NOT_TWO_LINES = "root@VS003#\n"
        + "root@VS003#\n"
        + "root@VS003#";
    private static final String OUTPUT_UNMATCHED = "root@VS004#\n"
        + "root@VS00X#";
    private static final String OUTPUT_EXPECTED = EXPECTED_PROMPT + "\n"
        + "\n"                              //ignore empty line
        + "[edit]\n"                        //ignore guidance line
        + "[edit interfaces ge-0/0/3]\n"    //ignore guidance line
        + "{master}\n"                      //ignore guidance line
        + "{master}[edit]\n"                //ignore guidance line
        + "{master}[edit interfaces ge-0/0/3]\n" //ignore guidance line
        + "{master:0}\n"                    //ignore guidance line
        + "{master:0}[edit]\n"              //ignore guidance line
        + "{master:0}[edit interfaces ge-0/0/3]\n" //ignore guidance line
        + "{backup}\n"                      //ignore guidance line
        + "{backup}[edit]\n"                //ignore guidance line
        + "{backup}[edit interfaces ge-0/0/3]\n" //ignore guidance line
        + "{backup:0}\n"                    //ignore guidance line
        + "{backup:0}[edit]\n"              //ignore guidance line
        + "{backup:0}[edit interfaces ge-0/0/3]\n" //ignore guidance line
        + EXPECTED_PROMPT;

    @Mock
    private Session session;

    @Mock
    private CompletionStage completion;

    @Mock
    private CompletableFuture future;

    private PromptResolutionStrategy target = JunosPromptResolutionStrategy.getInstance();

    private final String newline = "\n";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.doReturn(completion).when(session).write(Mockito.eq(newline + newline));
        Mockito.doReturn(future).when(completion).toCompletableFuture();
        Mockito.doReturn(null).when(future).get();
    }

    @Test
    public void testResolvePrompt() throws Exception {
        Mockito.doReturn(OUTPUT_EMPTY)
            .doReturn(OUTPUT_UNEXPECTED)
            .doReturn(OUTPUT_NOT_TWO_LINES)
            .doReturn(OUTPUT_UNMATCHED)
            .doReturn(OUTPUT_EXPECTED)
            .doThrow(new RuntimeException("the number of executions exceeded the assumed times.")) // never executed.
            .when(session).readUntilTimeout(Mockito.anyInt());

        // test
        String result = target.resolvePrompt(session, newline);

        //verify
        Assert.assertThat(result, CoreMatchers.equalTo(EXPECTED_PROMPT));

        Mockito.verify(session).readUntilTimeout(Mockito.eq(1));
        Mockito.verify(session).readUntilTimeout(Mockito.eq(2));
        Mockito.verify(session).readUntilTimeout(Mockito.eq(3));
        Mockito.verify(session).readUntilTimeout(Mockito.eq(4));
        Mockito.verify(session).readUntilTimeout(Mockito.eq(5));
        Mockito.verify(session, Mockito.never()).readUntilTimeout(Mockito.eq(6));
    }
}
