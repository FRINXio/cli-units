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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.cp;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPointsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.ConfigBuilder;

public class L2VsicpPointsWriterTest {

    @Test(expected = IllegalArgumentException.class)
    public void checkRemovedConnectionPointsTest() {
        L2vsicpPointsWriter.checkData(new ConnectionPointsBuilder().build(),
                "test", Optional.of(new NetworkInstanceBuilder().setName("").build()));
    }

    @Test(expected = NullPointerException.class)
    public void checkConnectionPointIdNotPresentTest() {
        L2vsicpPointsWriter.checkData(new ConnectionPointsBuilder().setConnectionPoint(Lists
                        .newArrayList(new ConnectionPointBuilder().build())).build(),
                "test", Optional.of(new NetworkInstanceBuilder().setName("").build()));
    }

    @Test(expected = NullPointerException.class)
    public void checkConnectionPointIdNotPresentInConfigTest() {
        L2vsicpPointsWriter.checkData(new ConnectionPointsBuilder().setConnectionPoint(Lists
                        .newArrayList(new ConnectionPointBuilder().setConnectionPointId("").build())).build(),
                "test", Optional.of(new NetworkInstanceBuilder().setName("").build()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkNINameNotEqualsWithConnectionPointIdTest() {
        L2vsicpPointsWriter.checkData(new ConnectionPointsBuilder().setConnectionPoint(Lists
                        .newArrayList(new ConnectionPointBuilder().setConnectionPointId("").setConfig(
                                new ConfigBuilder().setConnectionPointId("").build()).build())).build(),
                "test", Optional.of(new NetworkInstanceBuilder().setName("test").build()));
    }

    @Test
    public void checkNoExceptionTest() {
        L2vsicpPointsWriter.checkData(new ConnectionPointsBuilder().setConnectionPoint(Lists
                        .newArrayList(new ConnectionPointBuilder().setConnectionPointId("test").setConfig(
                                new ConfigBuilder().setConnectionPointId("test").build()).build())).build(),
                "test", Optional.of(new NetworkInstanceBuilder().setName("test").build()));
    }
}