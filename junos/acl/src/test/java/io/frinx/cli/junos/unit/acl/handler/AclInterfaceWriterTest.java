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

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.openconfig.openconfig.acl.IIDs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclInterfaceWriterTest {

    private AclInterfaceWriter target;
    @Mock
    private WriteContext writeContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new AclInterfaceWriter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteCurrentAttributesInvalidFormat() throws Exception {
        final String interfaceName = "ge-0/0/0";
        final InstanceIdentifier<Interface> instanceIdentifier = IIDs.AC_INTERFACES
            .child(Interface.class, new InterfaceKey(new InterfaceId(interfaceName)));
        final Interface dataAfter = Mockito.mock(Interface.class);

        target.writeCurrentAttributes(instanceIdentifier, dataAfter , writeContext);
    }

    @Test
    public void testWriteCurrentAttributesValidFormat() throws Exception {
        final String interfaceName = "ge-0/0/0.9999";
        final InstanceIdentifier<Interface> instanceIdentifier = IIDs.AC_INTERFACES
            .child(Interface.class, new InterfaceKey(new InterfaceId(interfaceName)));
        final Interface dataAfter = Mockito.mock(Interface.class);

        target.writeCurrentAttributes(instanceIdentifier, dataAfter , writeContext);
    }


}
