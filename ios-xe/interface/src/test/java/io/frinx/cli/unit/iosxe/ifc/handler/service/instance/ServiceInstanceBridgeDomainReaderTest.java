/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.BridgeDomainBuilder;

public class ServiceInstanceBridgeDomainReaderTest {

    @Test
    public void testFromEncapsulation() {
        final BridgeDomainBuilder builder = new BridgeDomainBuilder();
        ServiceInstanceBridgeDomainReader.parseBridgeDomain(ServiceInstanceConfigReaderTest.TRUNK_OUTPUT, builder);
        Assert.assertEquals("from-encapsulation", builder.getValue());
    }

    @Test
    public void testNumberWithGroup() {
        final BridgeDomainBuilder builder = new BridgeDomainBuilder();
        ServiceInstanceBridgeDomainReader.parseBridgeDomain(ServiceInstanceConfigReaderTest.EVC_OUTPUT, builder);
        Assert.assertEquals("200", builder.getValue());
        Assert.assertEquals(3, builder.getGroupNumber().shortValue());
    }

}