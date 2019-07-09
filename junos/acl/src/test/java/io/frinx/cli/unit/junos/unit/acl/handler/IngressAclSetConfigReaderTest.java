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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.acl.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetConfigReaderTest {
    private static final String INTERFACE_NAME = "ge-0/0/1";
    private static final String UNIT_NUMBER = "9999";

    private static final InstanceIdentifier<IngressAclSets> IIDS_INGRESS_ROOT = IIDs.AC_INTERFACES
        .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_NAME + "." + UNIT_NUMBER)))
        .child(IngressAclSets.class);

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private IngressAclSetConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new IngressAclSetConfigReader();
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        final String filterName = "FILTER-001";
        final Class<? extends ACLTYPE> type = ACLIPV4.class;
        final InstanceIdentifier<Config> iid = IIDS_INGRESS_ROOT
            .child(IngressAclSet.class, new IngressAclSetKey(filterName, type))
            .child(Config.class);
        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(iid, builder , readContext);

        Assert.assertThat(builder.getSetName(), CoreMatchers.sameInstance(filterName));
        Assert.assertThat(builder.getType(), CoreMatchers.sameInstance(type));
    }
}
