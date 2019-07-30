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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.oam.IIDs;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.DomainBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.DomainKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmDomainReaderTest {
    private static final String SH_RUN_LIST =
        "show running-config interface Bundle-Ether1000.100 ethernet cfm | include ^ {2}mep domain";
    private static final String SH_RUN_LIST_OUTPUT = "  mep domain DML1 service MA-001 mep-id 3\n"
        + "  mep domain DML2 service MA-002 mep-id 4 cos 1\n"
        + "  mep domain DML9 service MA-009 mep-id 9 loss-measurement counters aggregate\n"
        + "  mep domain DML4 service MA-004 mep-id 6 sla operation profile PPP target mep-id 2";

    private static final String SH_RUN =
        "show running-config interface Bundle-Ether1000.100 ethernet cfm | include ^ {2}mep domain DML1 ";
    private static final String SH_RUN_OUTPUT = "  mep domain DML1 service MA-001 mep-id 3\n";

    private static final String SH_RUN_DETAILS =
        "show running-config interface Bundle-Ether1000.100 ethernet cfm mep domain DML1 service MA-001 mep-id 3";
    private static final String SH_RUN_DETAILS_OUTPUT = "interface Bundle-Ether1000.100"
        + " ethernet cfm"
        + "  mep domain DML1 service MA-001 mep-id 3\n"
        + "   cos 1\n"
        + "  !\n"
        + " !\n"
        + "!\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private CfmDomainReader target;

    private static final String INTERFACE_NAME = "Bundle-Ether1000";
    private static final Long SUBIFC_INDEX = Long.valueOf(100L);

    private static final InterfaceKey INTERFACE_KEY = new InterfaceKey(INTERFACE_NAME);
    private static final SubinterfaceKey SUBIFC_KEY = new SubinterfaceKey(SUBIFC_INDEX);

    private static final String DOMAIN_NAME = "DML1";
    private static final DomainKey DOMAIN_KEY = new DomainKey(DOMAIN_NAME);
    private static final InstanceIdentifier<Domain> IID_FOR_LIST =
        IidUtils.createIid(IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DOMAINS, INTERFACE_KEY, SUBIFC_KEY)
        .child(Domain.class);
    private static final InstanceIdentifier<Domain> IID =
        IidUtils.createIid(IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DOMAIN, INTERFACE_KEY, SUBIFC_KEY, DOMAIN_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new CfmDomainReader(cli));
    }

    @Test
    public void testGetAllIds_001() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_LIST_OUTPUT).when(target)
            .blockingRead(SH_RUN_LIST, cli, IID_FOR_LIST, ctx);

        List<DomainKey> result = target.getAllIds(IID_FOR_LIST, ctx);

        Assert.assertThat(result, Matchers.containsInAnyOrder(
            new DomainKey("DML1"),
            new DomainKey("DML2"),
            new DomainKey("DML9"),
            new DomainKey("DML4")));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_OUTPUT).when(target)
            .blockingRead(SH_RUN, cli, IID, ctx);
        Mockito.doReturn(SH_RUN_DETAILS_OUTPUT).when(target)
            .blockingRead(SH_RUN_DETAILS, cli, IID, ctx);

        final DomainBuilder builder = new DomainBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getDomainName(), CoreMatchers.equalTo(DOMAIN_NAME));
        Assert.assertThat(builder.getConfig().getDomainName(), CoreMatchers.equalTo(DOMAIN_NAME));
        Assert.assertThat(builder.getMep().getConfig().getMaName(), CoreMatchers.equalTo("MA-001"));
        Assert.assertThat(builder.getMep().getConfig().getMepId(), CoreMatchers.equalTo(Integer.valueOf(3)));
        Assert.assertThat(builder.getMep().getConfig().getCos(), CoreMatchers.equalTo((short) 1));
    }
}
