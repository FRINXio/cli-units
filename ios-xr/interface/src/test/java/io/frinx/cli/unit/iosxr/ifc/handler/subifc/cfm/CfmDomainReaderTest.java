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
            "show running-config interface Bundle-Ether103.102 ethernet cfm";

    private static final String SH_RUN_LIST_OUTPUT_1 = "Tue Oct  8 06:10:39.968 UTC\n\r"
            + "interface Bundle-Ether103.102\n\r"
            + " ethernet cfm\n\r"
            + "  mep domain DML1 service MA-001 mep-id 3\n\r"
            + "  mep domain DML2 service MA-002 mep-id 4 cos 1\n\r"
            + "  mep domain DML9 service MA-009 mep-id 9 loss-measurement counters aggregate\n\r"
            + "  mep domain DML4 service MA-004 mep-id 6 sla operation profile PPP target mep-id 2\n\r"
            + "  !\n\r"
            + " !\n\r"
            + "!\n\r";

    private static final String SH_RUN_LIST_OUTPUT_2 = "Tue Oct  8 06:38:30.342 UTC\n\r"
            + "interface Bundle-Ether103.102\n\r"
            + " ethernet cfm\n\r"
            + "  mep domain DML1 service 502 mep-id 1\n\r"
            + "   cos 6\n\r"
            + "  mep domain DML2 service 503 mep-id 2\n\r"
            + "   cos 3\n\r"
            + "  mep domain DML3 service 504 mep-id 3\n\r"
            + "  !\n\r"
            + " !\n\r"
            + "!\n\r";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private CfmDomainReader target;

    private static final String INTERFACE_NAME = "Bundle-Ether103";
    private static final Long SUBIFC_INDEX = 102L;

    private static final InterfaceKey INTERFACE_KEY = new InterfaceKey(INTERFACE_NAME);
    private static final SubinterfaceKey SUBIFC_KEY = new SubinterfaceKey(SUBIFC_INDEX);

    private static final InstanceIdentifier<Domain> IID_FOR_LIST = IidUtils.createIid(
            IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DOMAINS, INTERFACE_KEY, SUBIFC_KEY).child(Domain.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new CfmDomainReader(cli));
    }

    @Test
    public void testGetAllIds_001() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_LIST_OUTPUT_1).when(target)
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
        final InstanceIdentifier<Domain> dml1 = getDomainIid("DML1");
        final InstanceIdentifier<Domain> dml2 = getDomainIid("DML2");
        final InstanceIdentifier<Domain> dml3 = getDomainIid("DML3");

        Mockito.doReturn(SH_RUN_LIST_OUTPUT_2).when(target).blockingRead(SH_RUN_LIST, cli, dml1, ctx);
        Mockito.doReturn(SH_RUN_LIST_OUTPUT_2).when(target).blockingRead(SH_RUN_LIST, cli, dml2, ctx);
        Mockito.doReturn(SH_RUN_LIST_OUTPUT_2).when(target).blockingRead(SH_RUN_LIST, cli, dml3, ctx);

        DomainBuilder builder = new DomainBuilder();
        target.readCurrentAttributes(dml1, builder, ctx);

        Assert.assertEquals(builder.getDomainName(), "DML1");
        Assert.assertEquals(builder.getMep().getConfig().getMaName(), "502");
        Assert.assertEquals(builder.getMep().getConfig().getMepId(), Integer.valueOf(1));
        Assert.assertEquals(builder.getMep().getConfig().getCos(), Short.valueOf("6"));

        builder = new DomainBuilder();
        target.readCurrentAttributes(dml2, builder, ctx);
        Assert.assertEquals(builder.getDomainName(), "DML2");
        Assert.assertEquals(builder.getMep().getConfig().getMaName(), "503");
        Assert.assertEquals(builder.getMep().getConfig().getMepId(), Integer.valueOf(2));
        Assert.assertEquals(builder.getMep().getConfig().getCos(), Short.valueOf("3"));

        builder = new DomainBuilder();
        target.readCurrentAttributes(dml3, builder, ctx);
        Assert.assertEquals(builder.getDomainName(), "DML3");
        Assert.assertEquals(builder.getMep().getConfig().getMaName(), "504");
        Assert.assertEquals(builder.getMep().getConfig().getMepId(), Integer.valueOf(3));
        Assert.assertNull(builder.getMep().getConfig().getCos());
    }

    private static InstanceIdentifier<Domain> getDomainIid(final String domainName) {
        return IidUtils.createIid(IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DOMAIN, INTERFACE_KEY,
                SUBIFC_KEY, new DomainKey(domainName));
    }
}
