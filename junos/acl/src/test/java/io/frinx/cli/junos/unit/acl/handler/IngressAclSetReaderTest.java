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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetReaderTest {
    private static final String INTERFACE_NAME = "ge-0/0/1";
    private static final String UNIT_NUMBER = "9999";
    private static final String SHOW_CONFIG = "show configuration interfaces " + INTERFACE_NAME
        + " unit " + UNIT_NUMBER + " family inet filter input | display set";

    private static final String OUTPUT_IFACE_FILTERS =
        "set interfaces " + INTERFACE_NAME + " unit " + UNIT_NUMBER + " family inet filter input FILTER01\n"
        + "set interfaces " + INTERFACE_NAME + " unit " + UNIT_NUMBER + " family inet filter input FILTER02\n";

    private static final List<IngressAclSetKey> EXPECTED_ACL_SET_KEY =
        Lists.newArrayList("FILTER01", "FILTER02").stream()
        .map(f -> new IngressAclSetKey(f, ACLIPV4.class))
        .collect(Collectors.toList());

    private static final InstanceIdentifier<IngressAclSets> IIDS_INGRESS_ROOT = IIDs.AC_INTERFACES
        .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_NAME + "." + UNIT_NUMBER)))
        .child(IngressAclSets.class);

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private IngressAclSetReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new IngressAclSetReader(cli));
    }

    @Test
    public void testGetAllIds() throws Exception {
        final InstanceIdentifier<IngressAclSet> iid = IIDS_INGRESS_ROOT
            .child(IngressAclSet.class);

        Mockito.doReturn(OUTPUT_IFACE_FILTERS).when(target)
                .blockingRead(
                    Mockito.eq(SHOW_CONFIG),
                    Mockito.eq(cli),
                    Mockito.eq(iid),
                    Mockito.eq(readContext));

        List<IngressAclSetKey> result = target.getAllIds(iid, readContext);

        Assert.assertThat(result, CoreMatchers.equalTo(EXPECTED_ACL_SET_KEY));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        final String filterName = "FILTER-001";
        final Class<? extends ACLTYPE> type = ACLIPV4.class;
        final InstanceIdentifier<IngressAclSet> iid = IIDS_INGRESS_ROOT
            .child(IngressAclSet.class, new IngressAclSetKey(filterName, type));
        final IngressAclSetBuilder builder = new IngressAclSetBuilder();

        target.readCurrentAttributes(iid, builder , readContext);

        Assert.assertThat(builder.getSetName(), CoreMatchers.sameInstance(filterName));
        Assert.assertThat(builder.getType(), CoreMatchers.sameInstance(type));
    }
}
