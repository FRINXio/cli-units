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

package io.frinx.cli.junos.unit.acl.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.acl.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclInterfaceConfigReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private AclInterfaceConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new AclInterfaceConfigReader());
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        final String interfaceId = "interface-id-001";
        final InstanceIdentifier<Config> iid = IIDs.AC_INTERFACES
            .child(Interface.class, new InterfaceKey(new InterfaceId(interfaceId)))
            .child(Config.class);
        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(iid, builder , readContext);

        Assert.assertThat(builder.getId().getValue(), CoreMatchers.sameInstance(interfaceId));
    }

}
