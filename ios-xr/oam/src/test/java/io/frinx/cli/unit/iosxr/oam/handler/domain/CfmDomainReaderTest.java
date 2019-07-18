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

package io.frinx.cli.unit.iosxr.oam.handler.domain;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.DomainBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.DomainKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmDomainReaderTest {
    private static final String SH_RUN_LIST = "show running-config ethernet cfm | include ^ domain";
    private static final String SH_RUN_LIST_OUTPUT = " domain DML1 level 3\n"
        + " domain DML2 level 4\n"
        + " domain DML9 level 9\n"
        + " domain DML4 level 6";

    private static final String SH_RUN = "show running-config ethernet cfm | include ^ domain DML1";
    private static final String SH_RUN_OUTPUT = "domain DML1 level 3\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private CfmDomainReader target;

    private static final String DOMAIN_NAME = "DML1";
    private static final DomainKey DOMAIN_KEY = new DomainKey(DOMAIN_NAME);
    private static final InstanceIdentifier<Domain> IID_FOR_LIST = IIDs.OA_CF_DO_DOMAIN;
    private static final InstanceIdentifier<Domain> IID = IidUtils.createIid(IIDs.OA_CF_DO_DOMAIN, DOMAIN_KEY);

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

        final DomainBuilder builder = new DomainBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getDomainName(), CoreMatchers.equalTo(DOMAIN_NAME));
    }
}
