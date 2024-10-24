/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.dasan.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Disabled
@ExtendWith(MockitoExtension.class)
class DasanCliUtilTest {

    private static String SHOW_ALL_PORTS;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        SHOW_ALL_PORTS = (String) FieldUtils.readStaticField(DasanCliUtil.class, "SHOW_ALL_PORTS", true);
    }

    @Test
    void testGetPhysicalPorts_001() throws Exception {

        final Cli cli = Mockito.mock(Cli.class);
        final CliReader<Config, ConfigBuilder> cliReader = Mockito.mock(CliReader.class);
        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Config.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String showportsStr = StringUtils.join(new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "1/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       N",
            "3/1       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
            "3/2       None      1     Up/Down  Force/Half/0    N/A/ Off       Y",
            "t/11  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            "t/12  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            }, "\n");
        final List<String> physportids = new ArrayList<>();
        Mockito.doReturn(showportsStr).when(cliReader).blockingRead(SHOW_ALL_PORTS, cli, id, readContext);
        try (MockedStatic<DasanCliUtil> mock = Mockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS)) {
            mock.when(() -> DasanCliUtil.parsePhysicalPorts(showportsStr)).thenReturn(physportids);
            List<String> result = DasanCliUtil.getPhysicalPorts(cli, cliReader, id, readContext);
            assertThat(result, CoreMatchers.sameInstance(physportids));
            verify(cliReader).blockingRead(SHOW_ALL_PORTS, cli, id, readContext);
            mock.verify(() -> DasanCliUtil.parsePhysicalPorts(Mockito.anyString()));
        }
    }

    @Test
    void testGetPhysicalPorts_002() throws Exception {

        final Cli cli = Mockito.mock(Cli.class);
        final CliReader<Config, ConfigBuilder> cliReader = Mockito.mock(CliReader.class);
        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Config.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String showportsStr = StringUtils.join(new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "1/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       N",
            "3/1       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
            "3/2       None      1     Up/Down  Force/Half/0    N/A/ Off       Y",
            "t/11  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            "t/12  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
        }, "\n");
        final ReadFailedException expectedException = new ReadFailedException(id, null);

        final List<String> physportids = new ArrayList<>();
        try (MockedStatic<DasanCliUtil> mock = Mockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS)) {
            Mockito.doThrow(expectedException).when(cliReader).blockingRead(SHOW_ALL_PORTS, cli, id, readContext);
            mock.when(() -> DasanCliUtil.parsePhysicalPorts(showportsStr)).thenReturn(physportids);

            ReadFailedException actualException = assertThrows(ReadFailedException.class, () ->
                DasanCliUtil.getPhysicalPorts(cli, cliReader, id, readContext)
            );

            assertSame(expectedException, actualException);

            verify(cliReader).blockingRead(SHOW_ALL_PORTS, cli, id, readContext);
            mock.verify(() -> DasanCliUtil.parsePhysicalPorts(Mockito.anyString()), never());
        }
    }


    @Test
    void testParsePhysicalPorts_001() {

        final String showportsStr = StringUtils.join(new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "1/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       N",
            "3/1       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
            "3/2       None      1     Up/Down  Force/Half/0    N/A/ Off       Y",
            "t/11  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            "t/12  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            }, "\n");


        List<String> result = DasanCliUtil.parsePhysicalPorts(showportsStr);

        assertThat(result.size(), CoreMatchers.is(8));
        assertThat(result.get(0), CoreMatchers.equalTo("1/1"));
        assertThat(result.get(1), CoreMatchers.equalTo("1/2"));
        assertThat(result.get(2), CoreMatchers.equalTo("2/1"));
        assertThat(result.get(3), CoreMatchers.equalTo("2/2"));
        assertThat(result.get(4), CoreMatchers.equalTo("3/1"));
        assertThat(result.get(5), CoreMatchers.equalTo("3/2"));
        assertThat(result.get(6), CoreMatchers.equalTo("t/11"));
        assertThat(result.get(7), CoreMatchers.equalTo("t/12"));

    }

    @Test
    void testContainsPort_001() {
        try (MockedStatic<DasanCliUtil> mock = Mockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS)) {
            final List<String> ports = new ArrayList<>();
            final String ranges = "1/1";
            final Set<String> parsedPort = Sets.newSet("1/1", "1/3");

            mock.when(() -> DasanCliUtil.parsePortRanges(ports, ranges)).thenReturn(parsedPort);

            assertThat(DasanCliUtil.containsPort(ports, ranges, "1/0"), CoreMatchers.is(false));
            assertThat(DasanCliUtil.containsPort(ports, ranges, "1/1"), CoreMatchers.is(true));
            assertThat(DasanCliUtil.containsPort(ports, ranges, "1/2"), CoreMatchers.is(false));
            assertThat(DasanCliUtil.containsPort(ports, ranges, "1/3"), CoreMatchers.is(true));
            assertThat(DasanCliUtil.containsPort(ports, ranges, "1/4"), CoreMatchers.is(false));

            mock.verify(() -> DasanCliUtil.parsePortRanges(Mockito.anyList(), Mockito.anyString()), Mockito.times(6));
        }
    }


    @Test
    void testParsePortRangesA_001() {
        final List<String> ports = Lists.newArrayList(
                "1/1", "1/2",
                "2/1", "2/2",
                "t/1", "t/2", "t/3");

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/0"),
                CoreMatchers.equalTo(Sets.newSet()));

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/1"),
                CoreMatchers.equalTo(Sets.newSet("1/1")));

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/2"),
                CoreMatchers.equalTo(Sets.newSet("1/2")));

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/3"),
                CoreMatchers.equalTo(Sets.newSet()));

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/1,2/2"),
                CoreMatchers.equalTo(Sets.newSet("1/1", "2/2")));

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/2-t/1"),
                CoreMatchers.equalTo(Sets.newSet("1/2", "2/1", "2/2", "t/1")));

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/1,2/2-t/1,t/3"),
                CoreMatchers.equalTo(Sets.newSet("1/1", "2/2", "t/1", "t/3")));
    }

    @Test
    void testContainsId_001() {
        try (MockedStatic<DasanCliUtil> mock = Mockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS)) {
            final String ranges = "2,4-6";
            final Set<String> parsedIds = Sets.newSet("2");

            mock.when(() -> DasanCliUtil.parseIdRanges(ranges)).thenReturn(parsedIds);

            assertThat(DasanCliUtil.containsId(ranges, "1"), CoreMatchers.is(false));
            assertThat(DasanCliUtil.containsId(ranges, "2"), CoreMatchers.is(true));

            mock.verify(() -> DasanCliUtil.parseIdRanges(Mockito.anyString()), Mockito.times(4));
        }
    }

    @Test
    void testParseIdRanges_001() {

        assertThat(DasanCliUtil.parseIdRanges("2"), CoreMatchers.equalTo(Sets.newSet("2")));
        assertThat(DasanCliUtil.parseIdRanges("4,6"), CoreMatchers.equalTo(Sets.newSet("4", "6")));
        assertThat(DasanCliUtil.parseIdRanges("8-10"), CoreMatchers.equalTo(Sets.newSet("8", "9", "10")));
        assertThat(DasanCliUtil.parseIdRanges("2,4-6,8-10"),
                CoreMatchers.equalTo(Sets.newSet("2", "4", "5", "6", "8", "9", "10")));
    }

    static class Config implements DataObject {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }

    static class ConfigBuilder implements Builder<Config> {
        @Override
        public Config build() {

            return null;
        }
    }
}
