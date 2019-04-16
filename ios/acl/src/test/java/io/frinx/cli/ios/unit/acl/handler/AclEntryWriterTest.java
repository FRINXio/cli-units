/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.ios.unit.acl.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.acl.entry.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;

public class AclEntryWriterTest {

    @Test
    public void processIpv6_srcDst_addresses() {
        AclEntry aclEntry = new AclEntryBuilder().setSequenceId(1L)
                .setConfig(new ConfigBuilder().setSequenceId(1L).build())
                .setIpv6(new Ipv6Builder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                    .rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                                .setDestinationAddress(new Ipv6Prefix("FE80:0000:0000:0000:0202:B3FF:FE1E:1111/96"))
                                .setSourceAddress(new Ipv6Prefix("FE80:0000:0000:0000:0202:B3FF:FE1E:2222/96"))
                                .build()
                ).build())
                .build();
        AclEntryWriter.MaxMetricCommandDTO result = new AclEntryWriter.MaxMetricCommandDTO();
        AclEntryWriter.processIpv6(aclEntry, result);
        Assert.assertEquals("FE80:0000:0000:0000:0202:B3FF:FE1E:1111/96", result.aclDstAddr);
        Assert.assertEquals("FE80:0000:0000:0000:0202:B3FF:FE1E:2222/96", result.aclSrcAddr);

        aclEntry = new AclEntryBuilder().setSequenceId(1L)
                .setConfig(new ConfigBuilder().setSequenceId(1L).build())
                .setIpv6(new Ipv6Builder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                    .rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                                .setDestinationAddress(new Ipv6Prefix("0:0:0:0:0:ffff:c000:0201/96"))
                                .setSourceAddress(new Ipv6Prefix("0:0:0:0:0:ffff:c000:0201/96"))
                                .build()
                ).build())
                .build();
        result = new AclEntryWriter.MaxMetricCommandDTO();
        AclEntryWriter.processIpv6(aclEntry, result);
        Assert.assertEquals("0:0:0:0:0:ffff:192.0.2.1/96", result.aclDstAddr);
        Assert.assertEquals("0:0:0:0:0:ffff:192.0.2.1/96", result.aclSrcAddr);

        aclEntry = new AclEntryBuilder().setSequenceId(1L)
                .setConfig(new ConfigBuilder().setSequenceId(1L).build())
                .setIpv6(new Ipv6Builder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields
                                    .rev171215.ipv6.protocol.fields.top.ipv6.ConfigBuilder()
                                .setDestinationAddress(new Ipv6Prefix("::ffff:c000:0201/96"))
                                .setSourceAddress(new Ipv6Prefix("::ffff:c000:0201/96"))
                                .build()
                ).build())
                .build();
        result = new AclEntryWriter.MaxMetricCommandDTO();
        AclEntryWriter.processIpv6(aclEntry, result);
        Assert.assertEquals("::ffff:192.0.2.1/96", result.aclDstAddr);
        Assert.assertEquals("::ffff:192.0.2.1/96", result.aclSrcAddr);
    }
}