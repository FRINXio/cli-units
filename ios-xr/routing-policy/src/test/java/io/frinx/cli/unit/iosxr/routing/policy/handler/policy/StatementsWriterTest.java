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

package io.frinx.cli.unit.iosxr.routing.policy.handler.policy;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;

public class StatementsWriterTest {

    @Rule
    public final ExpectedException ee = ExpectedException.none();

    private static final Statement S_ILLEGAL = new StatementBuilder()
            .setName("foo")
            .build();
    private static final Statement S1 = new StatementBuilder()
            .setName("1")
            .build();
    private static final Statement S2 = new StatementBuilder()
            .setName("2")
            .build();
    private static final Statement S3 = new StatementBuilder()
            .setName("3")
            .build();

    @Test
    public void sortByNames() throws Exception {
        List<Statement> sorted = StatementsWriter.sortByNames(ImmutableList.of(S2, S3, S1));
        Assert.assertEquals(ImmutableList.of(S1, S2, S3), sorted);
    }

    @Test
    public void sortByNames_IllegalName() throws Exception {
        ee.expect(IllegalArgumentException.class);
        ee.expectMessage("Statement name must be a number but was foo");
        StatementsWriter.sortByNames(ImmutableList.of(S2, S3, S_ILLEGAL));
    }

    @Test
    public void validateNames() throws Exception {
        // valid case
        StatementsWriter.validateNames(ImmutableList.of(S1, S2, S3));
        // non-valid case
        ee.expect(IllegalArgumentException.class);
        ee.expectMessage("Missing statement with name 2");
        StatementsWriter.validateNames(ImmutableList.of(S1, S3));
    }

}