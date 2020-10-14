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

package io.frinx.cli.unit.ios.qos.handler.scheduler;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder;

public class TwoRateThreeColorConfigReaderTest {

    private static final String OUTPUT = "  Policy Map TEST\n"
            + "    Class class-default\n"
            + "     police cir 500000000 bc 8000\n"
            + "       conform-action set-cos-transmit 2\n"
            + "       exceed-action drop \n";

    private ConfigBuilder builder;

    @Before
    public void setup() {
        this.builder = new ConfigBuilder();
    }

    @Test
    public void testCirAndBc() {
        TwoRateThreeColorConfigReader.fillInConfig(OUTPUT, "class-default", builder);
        Assert.assertEquals(new BigInteger("500000000"), builder.getCir());
        Assert.assertEquals(new Long("8000"), builder.getBc());
    }

}