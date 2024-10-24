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

import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class EgressAclSetReaderTest {
    private static final String INTERFACE_NAME = "ge-0/0/1";
    private static final String UNIT_NUMBER = "9999";

    private static final String OUTPUT_IFACE_FILTERS =
        "set interfaces " + INTERFACE_NAME + " unit " + UNIT_NUMBER + " family inet filter output FILTER01\n"
        + "set interfaces " + INTERFACE_NAME + " unit " + UNIT_NUMBER + " family inet filter output FILTER02\n";

    private static final List<EgressAclSetKey> EXPECTED_ACL_SET_KEY =
        Lists.newArrayList("FILTER01", "FILTER02").stream()
        .map(f -> new EgressAclSetKey(f, ACLIPV4.class))
        .collect(Collectors.toList());

    private static final InstanceIdentifier<EgressAclSets> IIDS_INGRESS_ROOT = IIDs.AC_INTERFACES
        .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_NAME + "." + UNIT_NUMBER)))
        .child(EgressAclSets.class);

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private EgressAclSetReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new EgressAclSetReader(cli);
    }

    @Test
    void testGetAllIds() throws Exception {
        final InstanceIdentifier<EgressAclSet> iid = IIDS_INGRESS_ROOT.child(EgressAclSet.class);
        Mockito.doReturn(CompletableFuture.completedFuture(OUTPUT_IFACE_FILTERS))
                .when(cli).executeAndRead(Mockito.any());

        List<EgressAclSetKey> result = target.getAllIds(iid, readContext);

        assertThat(result, CoreMatchers.equalTo(EXPECTED_ACL_SET_KEY));
    }

    @Test
    void testReadCurrentAttributes() throws Exception {
        final String filterName = "FILTER-001";
        final Class<? extends ACLTYPE> type = ACLIPV4.class;
        final InstanceIdentifier<EgressAclSet> iid = IIDS_INGRESS_ROOT
            .child(EgressAclSet.class, new EgressAclSetKey(filterName, type));
        final EgressAclSetBuilder builder = new EgressAclSetBuilder();

        target.readCurrentAttributes(iid, builder , readContext);

        assertThat(builder.getSetName(), CoreMatchers.sameInstance(filterName));
        assertThat(builder.getType(), CoreMatchers.sameInstance(type));
    }
}
