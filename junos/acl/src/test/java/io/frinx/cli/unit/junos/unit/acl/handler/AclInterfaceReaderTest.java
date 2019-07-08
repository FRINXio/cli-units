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

package io.frinx.cli.unit.junos.unit.acl.handler;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class AclInterfaceReaderTest {
    private static final String OUTPUT_IFACES = "set interfaces ge-0/0/1 unit 0 family inet filter input FILTER01\n"
        + "set interfaces ge-0/0/1 unit 0 family inet filter output FILTER02\n"
        + "set interfaces ge-0/0/2 unit 0 family inet filter input FILTER02\n"
        + "set interfaces ge-0/0/3 unit 0 family inet filter output FILTER02\n"
        + "set interfaces ge-0/0/4 unit 0 family inet filter unknown FILTER02\n";
    private static final List<String> EXPECTED_IFACES = Lists.newArrayList("ge-0/0/1.0", "ge-0/0/2.0", "ge-0/0/3.0");

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private AclInterfaceReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new AclInterfaceReader(cli));
    }

    @Test
    public void testGetAllIds() throws Exception {
        Mockito.doReturn(OUTPUT_IFACES).when(target)
                .blockingRead(
                    Mockito.eq(AclInterfaceReader.SH_IFACES),
                    Mockito.eq(cli),
                    Mockito.eq(IIDs.AC_IN_INTERFACE),
                    Mockito.eq(readContext));

        List<InterfaceKey> result = target.getAllIds(IIDs.AC_IN_INTERFACE, readContext);

        Assert.assertThat(result.stream()
            .map(m -> m.getId().getValue())
            .collect(Collectors.toList()), CoreMatchers.equalTo(EXPECTED_IFACES));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        final String interfaceId = "interface-id-001";
        final InstanceIdentifier<Interface> iid = IIDs.AC_INTERFACES
            .child(Interface.class, new InterfaceKey(new InterfaceId(interfaceId)));
        final InterfaceBuilder interfaceBuilder = new InterfaceBuilder();

        target.readCurrentAttributes(iid, interfaceBuilder , readContext);

        Assert.assertThat(interfaceBuilder.getId().getValue(), CoreMatchers.sameInstance(interfaceId));
    }
}
