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

import com.google.common.base.Optional;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.DomainKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.Ma;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.MaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.MaKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.types.rev190619.DomainLevel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmMaReaderTest {
    private static final String SH_RUN_LIST =
        "show running-config ethernet cfm domain DML1 level 3 | include ^ {2}service";
    private static final String SH_RUN_LIST_OUTPUT = "  service MA-001\n"
        + "  service MA-002 down-meps\n"
        + "  service MA-003 xconnect\n"
        + "  service MA-004 down-meps\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private CfmMaReader target;

    private static final String DOMAIN_NAME = "DML1";
    private static final String MA_NAME = "MA-002";
    private static final DomainKey DOMAIN_KEY = new DomainKey(DOMAIN_NAME);
    private static final MaKey MA_KEY = new MaKey(MA_NAME);
    private static final InstanceIdentifier<Ma> IID_FOR_LIST = IidUtils.createIid(IIDs.OA_CF_DO_DO_MAS, DOMAIN_KEY)
        .child(Ma.class);
    private static final InstanceIdentifier<Ma> IID = IidUtils.createIid(IIDs.OA_CF_DO_DO_MA_MA, DOMAIN_KEY, MA_KEY);

    private static final Config DOMAIN_CONFIG = new ConfigBuilder()
        .setDomainName(DOMAIN_NAME)
        .setLevel(new DomainLevel((short) 3))
        .build();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new CfmMaReader(cli));
    }

    @Test
    public void testGetAllIds_001() throws ReadFailedException {
        Mockito.doReturn(Optional.of(DOMAIN_CONFIG)).when(ctx).read(Mockito.any());
        Mockito.doReturn(SH_RUN_LIST_OUTPUT).when(target)
            .blockingRead(SH_RUN_LIST, cli, IID_FOR_LIST, ctx);

        List<MaKey> result = target.getAllIds(IID_FOR_LIST, ctx);

        Assert.assertThat(result, Matchers.containsInAnyOrder(
            new MaKey("MA-002"),
            new MaKey("MA-004")));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        final MaBuilder builder = new MaBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getMaName(), CoreMatchers.equalTo(MA_NAME));
    }
}
