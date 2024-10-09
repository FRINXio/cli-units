/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.saos8.platform.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaPlatformAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaSaos8PlatformAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.CienaSaos8PlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.CpuBoardBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.DeviceTechnologyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.DiagMonitorCapsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.DiagnosticMonitorTypeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.EnhancedOptBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.ExIdentifierBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.MainBoardBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.MediaPropertiesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.NumLanesSupportedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.RatesSupportedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.RxMonitorClockOptionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.SignalCodeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.TransceiverCodesPropsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.ciena.rev220620.ciena.saos8.platform.extension.TxMonitorClockOptionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Saos8ComponentStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_DEVICE_ID = "chassis device-id show";
    private static final String SH_PORTS = "port xcvr show";
    private static final String SH_PORT = "port xcvr show port %s vendor";

    private static final String PORT_VENDOR_NAME = "\\| %s *\\|"
            + "(?<vpn>[^\\|]+)\\|(?<eth>[^\\|]+)\\|(?<diag>[^\\|]+)\\|.*";

    private static final Pattern MOD_IDENTIFIER = Pattern
            .compile("\\| Ciena Module Identifier *\\|(?<mid>[^\\|]+)\\|.*\\|.*");
    private static final Pattern MOD_ITEM_NUM = Pattern
            .compile("\\| Ciena Module Item Number *\\|(?<min>[^\\|]+)\\|.*\\|.*");
    private static final Pattern MOD_REV_NUMBER = Pattern
            .compile("\\| Ciena Module Rev Number *\\|(?<mrn>[^\\|]+)\\|.*\\|.*");
    private static final Pattern VENDOR_SN = Pattern
            .compile("\\| Ciena Vendor Serial Num *\\|(?<vsn>[^\\|]+)\\|.*\\|.*");
    private static final Pattern DATE_CODE = Pattern
            .compile("\\| Date Code *\\|(?<dateCode>[^\\|]+)\\|.*\\|.*");
    private static final Pattern LOT_CODE = Pattern
            .compile("\\| Lot Code *\\|(?<lot>[^\\|]+)\\|.*\\|.*");
    private static final Pattern CLEI_CODE = Pattern
            .compile("\\| CLEI Code *\\|(?<clei>[^\\|]+)\\|.*\\|.*");

    private static final Pattern IDENTIFIER = Pattern.compile("\\| Identifier *\\|[^\\|]+\\|(?<id>[^\\|]+)\\|.*");
    private static final Pattern EXT_IDENTIFIER = Pattern
            .compile("\\| Ext. Identifier *\\|(?<ext>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern MODULE_POWER_LEVEL = Pattern
            .compile("\\| *- Module Power Level *\\|[^\\|]+\\|(?<mpl>[^\\|]+)\\|.*");
    private static final Pattern LANE_RATIO_TYPE = Pattern
            .compile("\\| *- Lane Ratio Type *\\|[^\\|]+\\|(?<rat>[^\\|]+)\\|.*");
    private static final Pattern WDM_TYPE = Pattern.compile("\\| *- WDM Type *\\|[^\\|]+\\|(?<wdm>[^\\|]+)\\|.*");
    private static final Pattern CLEI_CODE_PRESENT = Pattern
            .compile("\\| *- CLEI Code Present *\\|[^\\|]+\\|(?<clei>[^\\|]+)\\|.*");
    private static final Pattern CONNECTOR = Pattern.compile("\\| Connector *\\|[^\\|]+\\|(?<conn>[^\\|]+)\\|.*");

    private static final Pattern TRANSCEIVER_CODES = Pattern
            .compile("\\| Transceiver Codes *\\|(?<tc>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern ETHERNET_COMPLIANCE = Pattern
            .compile("\\| *- Ethernet Compliance *\\|[^\\|]+\\|(?<eth>[^\\|]+)\\|.*");
    private static final Pattern FIBER_COMPLIANCE = Pattern
            .compile("\\| *- Fiber Compliance *\\|[^\\|]+\\|(?<fib>[^\\|]+)\\|.*");
    private static final Pattern COPPER_COMPLIANCE = Pattern
            .compile("\\| *- Copper Compliance *\\|[^\\|]+\\|(?<cop>[^\\|]+)\\|.*");
    private static final Pattern SONET_COMPLIANCE = Pattern
            .compile("\\| *- SONET Compliance *\\|[^\\|]+\\|(?<sonet>[^\\|]+)\\|.*");
    private static final Pattern OTN_COMPLIANCE = Pattern
            .compile("\\| *- OTN Compliance *\\|[^\\|]+\\|(?<otn>[^\\|]+)\\|.*");

    private static final Pattern RATES_SUPPORTED = Pattern
            .compile("\\| Add'l Rates Supported *\\|(?<rs>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern RATE_111 = Pattern
            .compile("\\| *- 111.8 Gb/s *\\|[^\\|]+\\|(?<rat>[^\\|]+)\\|.*");
    private static final Pattern RATE_103 = Pattern
            .compile("\\| *- 103.125 Gb/s *\\|[^\\|]+\\|(?<rat>[^\\|]+)\\|.*");
    private static final Pattern RATE_41 = Pattern
            .compile("\\| *- 41.25 Gb/s *\\|[^\\|]+\\|(?<rat>[^\\|]+)\\|.*");
    private static final Pattern RATE_43 = Pattern
            .compile("\\| *- 43 Gb/s *\\|[^\\|]+\\|(?<rat>[^\\|]+)\\|.*");
    private static final Pattern RATE_39 = Pattern
            .compile("\\| *- 39.8 Gb/s *\\|[^\\|]+\\|(?<rat>[^\\|]+)\\|.*");

    private static final Pattern LANES_SUPPORTED = Pattern
            .compile("\\| Num Lanes Supported *\\|(?<lanes>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern NETWORK_LANES = Pattern
            .compile("\\| *- Num Network Lanes *\\|[^\\|]+\\| (?<lanes>\\d+) \\S+ *\\|.*");
    private static final Pattern HOST_LANES = Pattern
            .compile("\\| *- Num Host Lanes *\\|[^\\|]+\\| (?<lanes>\\d+) \\S+ *\\|.*");

    private static final Pattern MEDIA_PROPERTIES = Pattern
            .compile("\\| Media Properties *\\|(?<med>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern MEDIA_TYPE = Pattern
            .compile("\\| *- Media Type *\\|[^\\|]+\\|(?<medType>[^\\|]+)\\|.*");
    private static final Pattern DIRECTIONALITY = Pattern
            .compile("\\| *- Directionality *\\|[^\\|]+\\|(?<dir>[^\\|]+)\\|.*");
    private static final Pattern OPTICAL_MUX_DEMUX = Pattern
            .compile("\\| *- Optical Mux/Demux *\\|[^\\|]+\\|(?<optMuxDemux>[^\\|]+)\\|.*");
    private static final Pattern ACTIVE_FIBER_PER_CONN = Pattern
            .compile("\\| *- Active Fiber per Con *\\|[^\\|]+\\|(?<actFiber>[^\\|]+)\\|.*");

    private static final Pattern MAX_NETWORK_LANE_BR = Pattern
            .compile("\\| Max Network Lane BR *\\|[^\\|]+\\|(?<maxLane>[^\\|]+)\\|.*");
    private static final Pattern MAX_HOST_LANE_BR = Pattern
            .compile("\\| Max Host Lane BR *\\|[^\\|]+\\|(?<maxLane>[^\\|]+)\\|.*");
    private static final Pattern MAX_SM_FIBER_LENGTH = Pattern
            .compile("\\| Max SM Fiber Length *\\|[^\\|]+\\|(?<fibLength>[^\\|]+)\\|.*");
    private static final Pattern MAX_MM_FIBER_LENGTH = Pattern
            .compile("\\| Max MM Fiber Length *\\|[^\\|]+\\|(?<fibLength>[^\\|]+)\\|.*");
    private static final Pattern MAX_CU_CABLE_LENGTH = Pattern
            .compile("\\| Max Cu Cable Length *\\|[^\\|]+\\|(?<cabLength>[^\\|]+)\\|.*");
    private static final Pattern MIN_WAVELENGTH_PER_FIBER = Pattern
            .compile("\\| Min Wavelength per Fiber *\\|[^\\|]+\\|(?<minWave>[^\\|]+)\\|.*");
    private static final Pattern MAX_WAVELENGTH_PER_FIBER = Pattern
            .compile("\\| Max Wavelength per Fiber *\\|[^\\|]+\\|(?<maxWave>[^\\|]+)\\|.*");
    private static final Pattern MAX_PER_LANE_OPT_WIDTH = Pattern
            .compile("\\| Max per Lane Opt Width *\\|[^\\|]+\\|(?<optWidth>[^\\|]+)\\|.*");

    private static final Pattern DEVICE_TECHNOLOGY_1 = Pattern
            .compile("\\| Device Technology 1 *\\|(?<dev>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern LASER_SOURCE_TECH = Pattern
            .compile("\\| *- Laser Source Tech *\\|[^\\|]+\\|(?<laser>[^\\|]+)\\|.*");
    private static final Pattern TX_MODULATION_TECH = Pattern
            .compile("\\| *- Tx Modulation Tech *\\|[^\\|]+\\|(?<txMod>[^\\|]+)\\|.*");
    private static final Pattern DEVICE_TECHNOLOGY_2 = Pattern
            .compile("\\| Device Technology 2 *\\|(?<dev>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern WAVELENGTH_CONTROL = Pattern
            .compile("\\| *- Wavelength Control *\\|[^\\|]+\\|(?<waveControl>[^\\|]+)\\|.*");
    private static final Pattern COOLED_TR = Pattern
            .compile("\\| *- Cooled Transmitter *\\|[^\\|]+\\|(?<cooledTr>[^\\|]+)\\|.*");
    private static final Pattern TUNABILITY = Pattern
            .compile("\\| *- Tunability *\\|[^\\|]+\\|(?<tunability>[^\\|]+)\\|.*");
    private static final Pattern VOA_IMPL = Pattern
            .compile("\\| *- VOA Implemented *\\|[^\\|]+\\|(?<voa>[^\\|]+)\\|.*");
    private static final Pattern DETECTOR_TYPE = Pattern
            .compile("\\| *- Detector Type *\\|[^\\|]+\\|(?<det>[^\\|]+)\\|.*");
    private static final Pattern CDR_EDC = Pattern
            .compile("\\| *- CDR with EDC *\\|[^\\|]+\\|(?<cdrEdc>[^\\|]+)\\|.*");
    private static final Pattern SIG_CODE = Pattern
            .compile("\\| Signal Code *\\|(?<sigc>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern SIG_MODULATION = Pattern
            .compile("\\| *- Modulation *\\|[^\\|]+\\|(?<modulation>[^\\|]+)\\|.*");
    private static final Pattern SIG_CODING = Pattern
            .compile("\\| *- Signal Coding *\\|[^\\|]+\\|(?<sigc>[^\\|]+)\\|.*");

    private static final Pattern MAX_OUTPUT_PWR_PER_CON = Pattern
            .compile("\\| Max Output Pwr per Con *\\|[^\\|]+\\|(?<pwr>[^\\|]+)\\|.*");
    private static final Pattern MAX_INPUT_PWR_PER_LANE = Pattern
            .compile("\\| Max Input Pwr per Lane *\\|[^\\|]+\\|(?<pwr>[^\\|]+)\\|.*");
    private static final Pattern MAX_PWR_CONSUMPTION = Pattern
            .compile("\\| Max Pwr Consumption *\\|[^\\|]+\\|(?<pwr>[^\\|]+)\\|.*");
    private static final Pattern MAX_PWR_IN_LOW_PWR_MODE = Pattern
            .compile("\\| Max Pwr in Low Pwr Mode *\\|[^\\|]+\\|(?<pwr>[^\\|]+)\\|.*");
    private static final Pattern MAX_OPER_CASE_TEMP = Pattern
            .compile("\\| Max Oper Case Temp *\\|[^\\|]+\\|(?<temp>[^\\|]+)\\|.*");
    private static final Pattern MIN_OPER_CASE_TEMP = Pattern
            .compile("\\| Min Oper Case Temp *\\|[^\\|]+\\|(?<temp>[^\\|]+)\\|.*");
    private static final Pattern MAX_HIGH_POWER_UP_TIME = Pattern
            .compile("\\| Max High-Power-up Time *\\|[^\\|]+\\|(?<time>[^\\|]+)\\|.*");
    private static final Pattern MAX_HIGH_POWER_DOWN_TIME = Pattern
            .compile("\\| Max High-Power-down Time *\\|[^\\|]+\\|(?<time>[^\\|]+)\\|.*");
    private static final Pattern MAX_TX_TURN_ON_TIME = Pattern
            .compile("\\| Max Tx-Turn-on Time *\\|[^\\|]+\\|(?<time>[^\\|]+)\\|.*");
    private static final Pattern MAX_TX_TURN_OFF_TIME = Pattern
            .compile("\\| Max Tx-Turn-off Time *\\|[^\\|]+\\|(?<time>[^\\|]+)\\|.*");
    private static final Pattern HEAT_SINK_TYPE = Pattern
            .compile("\\| Heat Sink Type *\\|[^\\|]+\\|(?<type>[^\\|]+)\\|.*");
    private static final Pattern HOST_LN_SIG_SPEC = Pattern
            .compile("\\| Host Ln Signal Spec *\\|[^\\|]+\\|(?<spec>[^\\|]+)\\|.*");

    private static final Pattern CFP_MSA_HW_SPEC_REV = Pattern
            .compile("\\| CFP MSA HW Spec Rev *\\|[^\\|]+\\|(?<cfp>[^\\|]+)\\|.*");
    private static final Pattern CFP_MSA_MGMT_IF_SPEC_REV = Pattern
            .compile("\\| CFP MSA Mgmt IF Spec Rev *\\|[^\\|]+\\|(?<cfp>[^\\|]+)\\|.*");
    private static final Pattern MODULE_HW_VER = Pattern
            .compile("\\| Module HW Version *\\|[^\\|]+\\|(?<hwVer>[^\\|]+)\\|.*");
    private static final Pattern MODULE_FW_VER = Pattern
            .compile("\\| Module FW Version *\\|[^\\|]+\\|(?<fwVer>[^\\|]+)\\|.*");

    private static final Pattern DIAG_MONITOR_TYPE = Pattern
            .compile("\\| Diag Monitor Type *\\|(?<type>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern RX_PWR_MEAS_TYPE = Pattern
            .compile("\\| *- Rx Power Meas. Type *\\|[^\\|]+\\|(?<type>[^\\|]+)\\|.*");
    private static final Pattern TX_PWR_MEAS_TYPE = Pattern
            .compile("\\| *- Tx Power Meas. Type *\\|[^\\|]+\\|(?<type>[^\\|]+)\\|.*");
    private static final Pattern DIAG_MONITOR_CAPS_1 = Pattern
            .compile("\\| Diag Monitor Caps 1 *\\|(?<caps>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern TX_AUX_MONITOR_1 = Pattern
            .compile("\\| *- Tx aux monitor 1 *\\|[^\\|]+\\|(?<aux>[^\\|]+)\\|.*");
    private static final Pattern TX_AUX_MONITOR_2 = Pattern
            .compile("\\| *- Tx aux monitor 2 *\\|[^\\|]+\\|(?<aux>[^\\|]+)\\|.*");
    private static final Pattern TX_SOA_BIAS_CURRENT = Pattern
            .compile("\\| *- Tx SOA bias current *\\|[^\\|]+\\|(?<current>[^\\|]+)\\|.*");
    private static final Pattern TX_PWR_SUPPLY_VOLTAGE = Pattern
            .compile("\\| *- Tx pwr supply voltage *\\|[^\\|]+\\|(?<voltage>[^\\|]+)\\|.*");
    private static final Pattern TX_TEMPERATURE = Pattern
            .compile("\\| *- Tx temperature *\\|[^\\|]+\\|(?<temp>[^\\|]+)\\|.*");
    private static final Pattern DIAG_MONITOR_CAPS_2 = Pattern
            .compile("\\| Diag Monitor Caps 2 *\\|(?<caps>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern NET_LN_RX_PWR = Pattern
            .compile("\\| *- Netwk ln rx pwr *\\|[^\\|]+\\|(?<pwr>[^\\|]+)\\|.*");
    private static final Pattern NET_LN_OUT_PWR = Pattern
            .compile("\\| *- Netwk ln output pwr *\\|[^\\|]+\\|(?<pwr>[^\\|]+)\\|.*");
    private static final Pattern NET_LN_BIAS_CURRENT = Pattern
            .compile("\\| *- Netwk ln bias current *\\|[^\\|]+\\|(?<current>[^\\|]+)\\|.*");
    private static final Pattern NET_LN_TEMP = Pattern
            .compile("\\| *- Netwk ln temperature *\\|[^\\|]+\\|(?<temperature>[^\\|]+)\\|.*");

    private static final Pattern ENH_OPT_1 = Pattern
            .compile("\\| Enhanced Options 1 *\\|(?<opt>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern ENH_OPT_2 = Pattern
            .compile("\\| Enhanced Options 2 *\\|(?<opt>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern HOST_LN_LOOPBACK = Pattern
            .compile("\\| *- Host ln loopback *\\|[^\\|]+\\|(?<loopback>[^\\|]+)\\|.*");
    private static final Pattern HOST_LN_PRBS = Pattern
            .compile("\\| *- Host ln PRBS *\\|[^\\|]+\\|(?<prbs>[^\\|]+)\\|.*");
    private static final Pattern HOST_LN_EMP_CTRL = Pattern
            .compile("\\| *- Host ln emphasis ctrl *\\|[^\\|]+\\|(?<ctrl>[^\\|]+)\\|.*");
    private static final Pattern NET_LN_LOOPBACK = Pattern
            .compile("\\| *- Netwk ln loopback *\\|[^\\|]+\\|(?<loopback>[^\\|]+)\\|.*");
    private static final Pattern NET_LN_PRBS = Pattern
            .compile("\\| *- Netwk ln PRBS *\\|[^\\|]+\\|(?<prbs>[^\\|]+)\\|.*");
    private static final Pattern AMPL_ADJUST = Pattern
            .compile("\\| *- Amplitude adjustment *\\|[^\\|]+\\|(?<adjust>[^\\|]+)\\|.*");
    private static final Pattern PHASE_ADJ = Pattern
            .compile("\\| *- Phase adjustment *\\|[^\\|]+\\|(?<adjust>[^\\|]+)\\|.*");
    private static final Pattern UNIDIR_TX_RX = Pattern
            .compile("\\| *- Unidirectional tx/rx *\\|[^\\|]+\\|(?<unidir>[^\\|]+)\\|.*");
    private static final Pattern ACT_VOLT_PHA_FUNCT = Pattern
            .compile("\\| *- Active volt/phase func *\\|[^\\|]+\\|(?<function>[^\\|]+)\\|.*");
    private static final Pattern RX_FIFO_RESET = Pattern
            .compile("\\| *- Rx FIFO reset *\\|[^\\|]+\\|(?<reset>[^\\|]+)\\|.*");
    private static final Pattern RX_FIFO_AUTO_RESET = Pattern
            .compile("\\| *- Rx FIFO auto reset *\\|[^\\|]+\\|(?<reset>[^\\|]+)\\|.*");
    private static final Pattern TX_FIFO_RESET = Pattern
            .compile("\\| *- Tx FIFO reset *\\|[^\\|]+\\|(?<reset>[^\\|]+)\\|.*");
    private static final Pattern TX_FIFO_AUTO_RESET = Pattern
            .compile("\\| *- Tx FIFO auto reset *\\|[^\\|]+\\|(?<reset>[^\\|]+)\\|.*");

    private static final Pattern TX_RX_MONITOR = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern TX_MONITOR_16_H = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/16 of Host ln rate *\\|[^\\|]+\\|(?<hostLnRate16TX>[^\\|]+)\\|.*)"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern TX_MONITOR_16_N = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/16 of Netwk ln rate *\\|[^\\|]+\\|(?<netLnRate16TX>[^\\|]+)\\|.*)"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern TX_MONITOR_64_H = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/64 of Host ln rate *\\|[^\\|]+\\|(?<hostLnRate64TX>[^\\|]+)\\|.*)"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern TX_MONITOR_64_N = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/64 of Netwk ln rate *\\|[^\\|]+\\|(?<netLnRate64TX>[^\\|]+)\\|.*)"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern TX_MONITOR_8 = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/8 of Netwk ln rate *\\|[^\\|]+\\|(?<netLnRate8TX>[^\\|]+)\\|.*)"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern TX_MONITOR_CLOCK = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- Monitor clock option *\\|[^\\|]+\\|(?<monitorOptionTX>[^\\|]+)\\|.*)"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*");
    private static final Pattern RX_MONITOR_16_H = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/16 of Host ln rate *\\|[^\\|]+\\|(?<hostLnRate16RX>[^\\|]+)\\|.*)");
    private static final Pattern RX_MONITOR_16_N = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/16 of Netwk ln rate *\\|[^\\|]+\\|(?<netLnRate16RX>[^\\|]+)\\|.*)");
    private static final Pattern RX_MONITOR_64_H = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/64 of Host ln rate *\\|[^\\|]+\\|(?<hostLnRate64RX>[^\\|]+)\\|.*)");
    private static final Pattern RX_MONITOR_64_N = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/64 of Netwk ln rate *\\|[^\\|]+\\|(?<netLnRate64RX>[^\\|]+)\\|.*)");
    private static final Pattern RX_MONITOR_8 = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- 1/8 of Netwk ln rate *\\|[^\\|]+\\|(?<netLnRate8RX>[^\\|]+)\\|.*)");
    private static final Pattern RX_MONITOR_CLOCK = Pattern
            .compile(".*\\| Tx Monitor Clock Options *\\|(?<txMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "\\| Rx Monitor Clock Options *\\|(?<rxMonitor>[^\\|]+)\\|[^\\|]+\\|.*"
                    + "(\\| *- Monitor clock option *\\|[^\\|]+\\|(?<monitorOptionRX>[^\\|]+)\\|.*)");

    private static final Pattern PARSE_DEVICE = Pattern.compile("\\+-+ CHASSIS DEVICE ID .*"
            + "\\| Ethernet Base Address *\\| (?<ethBaseAddress>[^|]+)\\|.*"
            + "\\| Eth Address Block Size *\\| (?<ethAddrBlockSize>[^|]+)\\|.*"
            + "\\| Module Serial Number *\\| (?<moduleSN>[^|]+)\\|.*"
            + "\\| Model Part Number *\\| (?<modelPN>[^|]+)\\|.*"
            + "\\| Model Revision *\\| (?<modelRev>[^|]+)\\|.*"
            + "\\| Product ID *\\| (?<pid>[^|]+)\\|.*"
            + "\\| Manufactured Date *\\| (?<manufactDate>[^|]+)\\|.*"
            + "\\| CLEI Code *\\| (?<CLEI>[^|]+)\\|.*"
            + "\\| Bar Code *\\| (?<barcode>[^|]+)\\|.*"
            + "\\| Backplane Assy Serial Num *\\| (?<backplAssySN>[^|]+)\\|.*"
            + "\\| Backplane Assy Part Number *\\| (?<backplAssyPN>[^|]+)\\|.*"
            + "\\| Backplane Assy Revision *\\| (?<backplAssyRev>[^|]+)\\|.*"
            + "\\| Backplane Serial Number *\\| (?<backplaneSN>[^|]+)\\|.*"
            + "\\| Backplane Part Number *\\| (?<backplanePN>[^|]+)\\|.*"
            + "\\| Backplane Revision *\\| (?<backplaneRev>[^|]+)\\|.*"
            + "\\| Software Compatibility *\\| (?<softCompat>[^|]+)\\|.*"
            + "\\| Functional Test Count *\\| (?<functTestCnt>[^|]+)\\|.*");

    private static final Pattern PARSE_IOM = Pattern.compile("\\+-+ IOM DEVICE ID .*"
            + "\\| Module Serial Number *\\| (?<moduleSN>[^|]+)\\|.*"
            + "\\| Model Part Number *\\| (?<modelPN>[^|]+)\\|.*"
            + "\\| Model Revision *\\| (?<modelRev>[^|]+)\\|.*"
            + "\\| Product ID *\\| (?<productID>[^|]+)\\|.*"
            + "\\| Manufactured Date *\\| (?<manufDate>[^|]+)\\|.*"
            + "\\| CLEI Code *\\| (?<clei>[^|]+)\\|.*"
            + "\\| Bar Code *\\| (?<barcode>[^|]+)\\|.*"
            + "\\| Board Serial Number *\\| (?<boardSN>[^|]+)\\|.*"
            + "\\| Board Part Number *\\| (?<boardPN>[^|]+)\\|.*"
            + "\\| Board Revision *\\| (?<boardRev>[^|]+)\\|.*"
            + "\\| Software Compatibility *\\| (?<softCompat>[^|]+)\\|.*"
            + "\\| Functional Test Count *\\| (?<functTestCnt>[^|]+)\\|.*");

    private static final String PARSE_DEV_SLOT = "\\+-+ DEVICE ID SLOT %s .*"
            + "\\| Module Serial Number *\\| (?<moduleSN>[^|]+)\\|.*"
            + "\\| Model Part Number *\\| (?<modelPN>[^|]+)\\|.*"
            + "\\| Model Revision *\\| (?<modelRev>[^|]+)\\|.*"
            + "\\| Product ID *\\| (?<pid>[^|]+)\\|.*"
            + "\\| Manufactured Date *\\| (?<manufDate>[^|]+)\\|.*"
            + "\\| CLEI Code *\\| (?<clei>[^|]+)\\|.*"
            + "\\| Bar Code *\\| (?<barcode>[^|]+)\\|.*"
            + "\\| Board Serial Number *\\| (?<boardSN>[^|]+)\\|.*"
            + "\\| Board Part Number *\\| (?<boardPN>[^|]+)\\|.*"
            + "\\| Board Revision *\\| (?<boardRev>[^|]+)\\|.*"
            + "\\| Software Compatibility *\\| (?<softCompat>[^|]+)\\|.*"
            + "\\| Functional Test Count *\\| (?<functTestCnt>[^|]+)\\|.*";

    private static final String PARSE_MOD_SLOT = "\\+-+ MODULE DEVICE ID SLOT %s .*"
            + "\\| Module Serial Number *\\| (?<moduleSN>[^|]+)\\|.*"
            + "\\| Model Part Number *\\| (?<modelPN>[^|]+)\\|.*"
            + "\\| Model Revision *\\| (?<modelRev>[^|]+)\\|.*"
            + "\\| Product ID *\\| (?<productId>[^|]+)\\|.*"
            + "\\| Manufactured Date *\\| (?<manufDate>[^|]+)\\|.*"
            + "\\| CLEI Code *\\| (?<clei>[^|]+)\\|.*"
            + "\\| Bar Code *\\| (?<barcode>[^|]+)\\|.*"
            + "\\| Board Serial Number *\\| (?<boardSN>[^|]+)\\|.*"
            + "\\| Board Part Number *\\| (?<boardPN>[^|]+)\\|.*"
            + "\\| Board Revision *\\| (?<boardRev>[^|]+)\\|.*"
            + "\\| Software Compatibility *\\| (?<softCompat>[^|]+)\\|.*"
            + "\\| Functional Test Count *\\| (?<functTestCnt>[^|]+)\\|.*"
            + "\\| Fault Card *\\| (?<faultCard>[^|]+)\\|.*";

    private static final String PARSE_MOD_SLOT_CTM = "\\+-+ MODULE DEVICE ID SLOT %s .*"
            + "\\| Ethernet Base Address *\\| (?<ethBaseAddr>[^|]+)\\|.*"
            + "\\| Eth Address Block Size *\\| (?<ethAddrBlockSize>[^|]+)\\|.*";

    private static final String PARSE_MOD_SLOT_CPU = "\\+-+ MODULE DEVICE ID SLOT %s .*"
            + "\\| CPU Board *\\| *\\|.*"
            + "\\|  Ethernet Base Address *\\| (?<ethBaseAddr>[^|]+)\\|.*"
            + "\\|  Eth Address Block Size *\\| (?<ethAddrBlockSize>[^|]+)\\|.*"
            + "\\|  Module Serial Number *\\| (?<moduleSN>[^|]+)\\|.*"
            + "\\|  Model Part Number *\\| (?<modelPN>[^|]+)\\|.*"
            + "\\|  Model Revision *\\| (?<modelRev>[^|]+)\\|.*"
            + "\\|  Product ID *\\| (?<pid>[^|]+)\\|.*"
            + "\\|  Manufactured Date *\\| (?<manufDate>[^|]+)\\|.*"
            + "\\|  CLEI Code *\\| (?<clei>[^|]+)\\|.*"
            + "\\|  Bar Code *\\| (?<barcode>[^|]+)\\|.*"
            + "\\|  Board Serial Number *\\| (?<boardSN>[^|]+)\\|.*"
            + "\\|  Board Part Number *\\| (?<boardPN>[^|]+)\\|.*"
            + "\\|  Board Revision *\\| (?<boardRev>[^|]+)\\|.*"
            + "\\|  Software Compatibility *\\| (?<softCompat>[^|]+)\\|.*"
            + "\\|  Functional Test Count *\\| (?<functTestCnt>[^|]+)\\|.*"
            + "\\|  Fault Card *\\| (?<faultCard>[^|]+)\\|.*"
            + "\\| Main Board *.*";

    private static final String PARSE_MOD_SLOT_MAIN = "\\+-+ MODULE DEVICE ID SLOT %s .*"
            + "\\| CPU Board *\\| *\\|.*"
            + "\\| Main Board *\\| *\\|.*"
            + "\\|  Module Serial Number *\\| (?<moduleSN>[^|]+)\\|.*"
            + "\\|  Model Part Number *\\| (?<modelPN>[^|]+)\\|.*"
            + "\\|  Model Revision *\\| (?<modelRev>[^|]+)\\|.*"
            + "\\|  Manufactured Date *\\| (?<manufDate>[^|]+)\\|.*"
            + "\\|  Board Serial Number *\\| (?<boardSN>[^|]+)\\|.*"
            + "\\|  Board Part Number *\\| (?<boardPN>[^|]+)\\|.*"
            + "\\|  Board Revision *\\| (?<boardRev>[^|]+)\\|.*";

    private final Cli cli;

    public Saos8ComponentStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var name = id.firstKeyOf(Component.class).getName();
        var builder = new CienaSaos8PlatformAugBuilder();
        var cienaBuilder = new CienaPlatformAugBuilder();
        if (name.equals(Saos8ComponentReader.DEVICE_ID)) {
            parseDev(stateBuilder, builder, blockingRead(f(SH_DEVICE_ID), cli, id, readContext));
        } else if (name.startsWith(Saos8ComponentReader.MODULE_PREFIX)) {
            var module = name.replaceFirst(Saos8ComponentReader.MODULE_PREFIX, "");
            if (module.startsWith("LM")) {
                parseModuleSlotLM(stateBuilder, builder, blockingRead(f(SH_DEVICE_ID), cli, id, readContext), module);
            } else {
                parseModuleSlot(stateBuilder, builder, blockingRead(f(SH_DEVICE_ID), cli, id, readContext), module);
            }
        } else if (name.startsWith(Saos8ComponentReader.DEVICE_PREFIX)) {
            var dev = name.replaceFirst(Saos8ComponentReader.DEVICE_PREFIX, "");
            parseDeviceSlot(stateBuilder, builder, blockingRead(f(SH_DEVICE_ID), cli, id, readContext), dev);
        } else if (name.startsWith(Saos8ComponentReader.IOM_PREFIX)) {
            var iom = name.replaceFirst(Saos8ComponentReader.IOM_PREFIX, "");
            parseIom(stateBuilder, builder, blockingRead(f(SH_DEVICE_ID), cli, id, readContext), iom);
        } else if (name.startsWith(Saos8ComponentReader.PORT_PREFIX_CONST)) {
            var port = name.replaceFirst(Saos8ComponentReader.PORT_PREFIX_CONST, "");
            parsePort(stateBuilder, builder, cienaBuilder, blockingRead(f(SH_PORT, port), cli, id, readContext)
                    + blockingRead(f(SH_PORTS), cli, id, readContext), port);
        }
        stateBuilder.addAugmentation(CienaSaos8PlatformAug.class, builder.build());
        stateBuilder.addAugmentation(CienaPlatformAug.class, cienaBuilder.build());
    }

    static void parseDev(@NotNull StateBuilder stateBuilder,
                         CienaSaos8PlatformAugBuilder builder,
                         String output) {
        output = processOutput(output, false);
        stateBuilder.setName(Saos8ComponentReader.DEVICE_ID);
        stateBuilder.setId(Saos8ComponentReader.DEVICE_ID);

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("ethBaseAddress"),
            v -> builder.setEthernetBaseAddress(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("ethAddrBlockSize"),
            v -> builder.setEthernetAddressBlockSize(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("moduleSN"),
            v -> builder.setModuleSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("modelPN"),
            v -> builder.setModelPartNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("modelRev"),
            v -> builder.setModelRevision(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("pid"),
            v -> builder.setProductId(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("manufactDate"),
            v -> builder.setManufacturedDate(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("CLEI"),
            v -> builder.setCleiCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("barcode"),
            v -> builder.setBarCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("backplAssySN"),
            v -> builder.setBackplaneAssySn(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("backplAssyPN"),
            v -> builder.setBackplaneAssyPn(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("backplAssyRev"),
            v -> builder.setBackplaneAssyRev(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("backplaneSN"),
            v -> builder.setBackplaneSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("backplanePN"),
            v -> builder.setBackplanePn(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("backplaneRev"),
            v -> builder.setBackplaneRev(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("softCompat"),
            v -> builder.setSoftwareCompatibility(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_DEVICE::matcher,
            m -> m.group("functTestCnt"),
            v -> builder.setFunctionalTestCount(v.trim()));
    }

    static void parseModuleSlot(@NotNull StateBuilder stateBuilder,
                                CienaSaos8PlatformAugBuilder builder,
                                String output, String name) {
        output = processOutput(output, false);
        stateBuilder.setName(name);
        stateBuilder.setId("MODULE");
        var parseModSlot = Pattern.compile(String.format(PARSE_MOD_SLOT, name));
        var parseModSlotCtm = Pattern.compile(String.format(PARSE_MOD_SLOT_CTM, name));

        ParsingUtils.parseField(output, 0,
            parseModSlotCtm::matcher,
            m -> m.group("ethBaseAddr"),
            v -> builder.setEthernetBaseAddress(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlotCtm::matcher,
            m -> m.group("ethAddrBlockSize"),
            v -> builder.setEthernetAddressBlockSize(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("moduleSN"),
            v -> builder.setModuleSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("modelPN"),
            v -> builder.setModelPartNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("modelRev"),
            v -> builder.setModelRevision(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("productId"),
            v -> builder.setProductId(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("manufDate"),
            v -> builder.setManufacturedDate(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("clei"),
            v -> builder.setCleiCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("barcode"),
            v -> builder.setBarCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("boardSN"),
            v -> builder.setBoardSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("boardPN"),
            v -> builder.setBoardPn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("boardRev"),
            v -> builder.setBoardRev(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("softCompat"),
            v -> builder.setSoftwareCompatibility(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("functTestCnt"),
            v -> builder.setFunctionalTestCount(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModSlot::matcher,
            m -> m.group("faultCard"),
            v -> builder.setFaultCard(v.trim()));
    }

    static void parseModuleSlotLM(@NotNull StateBuilder stateBuilder,
                                    CienaSaos8PlatformAugBuilder builder,
                                    String output, String name) {
        output = processOutput(output, false);
        stateBuilder.setName(name);
        stateBuilder.setId("MODULE");
        var cpuBuilder = new CpuBoardBuilder();
        var mainBuilder = new MainBoardBuilder();
        var parseModMain = Pattern.compile(String.format(PARSE_MOD_SLOT_MAIN, name));
        var parseModCPU = Pattern.compile(String.format(PARSE_MOD_SLOT_CPU, name));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("ethBaseAddr"),
            v -> cpuBuilder.setEthernetBaseAddress(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("ethAddrBlockSize"),
            v -> cpuBuilder.setEthernetAddressBlockSize(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("moduleSN"),
            v -> cpuBuilder.setModuleSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("modelPN"),
            v -> cpuBuilder.setModelPartNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("modelRev"),
            v -> cpuBuilder.setModelRevision(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("pid"),
            v -> cpuBuilder.setProductId(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("manufDate"),
            v -> cpuBuilder.setManufacturedDate(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("clei"),
            v -> cpuBuilder.setCleiCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("barcode"),
            v -> cpuBuilder.setBarCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("boardSN"),
            v -> cpuBuilder.setBoardSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("boardPN"),
            v -> cpuBuilder.setBoardPn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("boardRev"),
            v -> cpuBuilder.setBoardRev(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("softCompat"),
            v -> cpuBuilder.setSoftwareCompatibility(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("functTestCnt"),
            v -> cpuBuilder.setFunctionalTestCount(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModCPU::matcher,
            m -> m.group("faultCard"),
            v -> cpuBuilder.setFaultCard(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModMain::matcher,
            m -> m.group("moduleSN"),
            v -> mainBuilder.setModuleSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModMain::matcher,
            m -> m.group("modelPN"),
            v -> mainBuilder.setModelPartNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModMain::matcher,
            m -> m.group("modelRev"),
            v -> mainBuilder.setModelRevision(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModMain::matcher,
            m -> m.group("manufDate"),
            v -> mainBuilder.setManufacturedDate(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModMain::matcher,
            m -> m.group("boardSN"),
            v -> mainBuilder.setBoardSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModMain::matcher,
            m -> m.group("boardPN"),
            v -> mainBuilder.setBoardPn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseModMain::matcher,
            m -> m.group("boardRev"),
            v -> mainBuilder.setBoardRev(v.trim()));

        builder.setCpuBoard(cpuBuilder.build());
        builder.setMainBoard(mainBuilder.build());
    }

    static void parseDeviceSlot(@NotNull StateBuilder stateBuilder,
                                CienaSaos8PlatformAugBuilder builder,
                                String output, String name) {
        output = processOutput(output, false);
        stateBuilder.setName(name);
        stateBuilder.setId("DEV-SLOT");
        var parseDevSlot = Pattern.compile(String.format(PARSE_DEV_SLOT, name));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("moduleSN"),
            v -> builder.setModuleSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("modelPN"),
            v -> builder.setModelPartNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("modelRev"),
            v -> builder.setModelRevision(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("pid"),
            v -> builder.setProductId(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("manufDate"),
            v -> builder.setManufacturedDate(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("clei"),
            v -> builder.setCleiCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("barcode"),
            v -> builder.setBarCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("boardSN"),
            v -> builder.setBoardSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("boardPN"),
            v -> builder.setBoardPn(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("boardRev"),
            v -> builder.setBoardRev(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("softCompat"),
            v -> builder.setSoftwareCompatibility(v.trim()));

        ParsingUtils.parseField(output, 0,
            parseDevSlot::matcher,
            m -> m.group("functTestCnt"),
            v -> builder.setFunctionalTestCount(v.trim()));
    }

    static void parseIom(@NotNull StateBuilder stateBuilder,
                         CienaSaos8PlatformAugBuilder builder,
                         String output, String name) {
        output = processOutput(output, false);
        stateBuilder.setName(name);
        stateBuilder.setId("IOM");

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("moduleSN"),
            v -> builder.setModuleSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("modelPN"),
            v -> builder.setModelPartNumber(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("modelRev"),
            v -> builder.setModelRevision(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("productID"),
            v -> builder.setProductId(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("manufDate"),
            v -> builder.setManufacturedDate(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("clei"),
            v -> builder.setCleiCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("barcode"),
            v -> builder.setBarCode(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("boardSN"),
            v -> builder.setBoardSn(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("boardPN"),
            v -> builder.setBoardPn(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("boardRev"),
            v -> builder.setBoardRev(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("softCompat"),
            v -> builder.setSoftwareCompatibility(v.trim()));

        ParsingUtils.parseField(output, 0,
            PARSE_IOM::matcher,
            m -> m.group("functTestCnt"),
            v -> builder.setFunctionalTestCount(v.trim()));
    }

    static void parsePort(@NotNull StateBuilder stateBuilder,
                          CienaSaos8PlatformAugBuilder builder,
                          CienaPlatformAugBuilder cienaBuilder,
                          String output, String name) {
        stateBuilder.setName(name);
        stateBuilder.setId("Port");

        ParsingUtils.parseField(output, 0,
            Pattern.compile(String.format(PORT_VENDOR_NAME, name))::matcher,
            m -> m.group("vpn"),
            v -> builder.setEmpty(v.trim().equals("Empty")));

        if (!builder.isEmpty()) {
            final var oneLineOutput = processOutput(output, true);
            final var extIdBuilder = new ExIdentifierBuilder();
            final var transceiverCodesBuilder = new TransceiverCodesPropsBuilder();
            final var ratesBuilder = new RatesSupportedBuilder();
            final var supportedLanesBuilder = new NumLanesSupportedBuilder();
            final var mediaPropBuilder = new MediaPropertiesBuilder();
            final var deviceTechBuilder = new DeviceTechnologyBuilder();
            final var sigCodeBuilder = new SignalCodeBuilder();
            final var diagMonitorTypeBuilder = new DiagnosticMonitorTypeBuilder();
            final var diagMonitorCapsBuilder = new DiagMonitorCapsBuilder();
            final var enhancedOptBuilder = new EnhancedOptBuilder();
            final var txMonitorOptBuilder = new TxMonitorClockOptionsBuilder();
            final var rxMonitorOptBuilder = new RxMonitorClockOptionsBuilder();

            ParsingUtils.parseField(output, 0,
                Pattern.compile(String.format(PORT_VENDOR_NAME, name))::matcher,
                m -> m.group("vpn"),
                v -> builder.setVendorPartNumber(v.trim()));

            ParsingUtils.parseField(output, 0,
                Pattern.compile(String.format(PORT_VENDOR_NAME, name))::matcher,
                m -> m.group("eth"),
                v -> builder.setEthernetType(v.trim()));

            ParsingUtils.parseField(output, 0,
                Pattern.compile(String.format(PORT_VENDOR_NAME, name))::matcher,
                m -> m.group("diag"),
                v -> builder.setDiagnosticData(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                MOD_IDENTIFIER::matcher,
                m -> m.group("mid"),
                v -> builder.setCienaModuleIdentifier(v.trim()));

            ParsingUtils.parseField(output, 0,
                MOD_ITEM_NUM::matcher,
                m -> m.group("min"),
                v -> builder.setCienaModuleItemNumber(v.trim()));

            ParsingUtils.parseField(output, 0,
                MOD_REV_NUMBER::matcher,
                m -> m.group("mrn"),
                v -> builder.setCienaModuleRevNumber(v.trim()));

            ParsingUtils.parseField(output, 0,
                VENDOR_SN::matcher,
                m -> m.group("vsn"),
                v -> builder.setCienaVendorSerialNumber(v.trim()));

            ParsingUtils.parseField(output, 0,
                DATE_CODE::matcher,
                m -> m.group("dateCode"),
                v -> builder.setDateCode(v.trim()));

            ParsingUtils.parseField(output, 0,
                LOT_CODE::matcher,
                m -> m.group("lot"),
                v -> builder.setLotCode(v.trim()));

            ParsingUtils.parseField(output, 0,
                CLEI_CODE::matcher,
                m -> m.group("clei"),
                v -> builder.setCleiCode(v.trim()));

            ParsingUtils.parseField(output, 0,
                IDENTIFIER::matcher,
                m -> m.group("id"),
                v -> cienaBuilder.setIdentifier(v.trim()));

            ParsingUtils.parseField(output, 0,
                EXT_IDENTIFIER::matcher,
                m -> m.group("ext"),
                v -> extIdBuilder.setExtIdentifier(v.trim()));

            ParsingUtils.parseField(output, 0,
                MODULE_POWER_LEVEL::matcher,
                m -> m.group("mpl"),
                v -> extIdBuilder.setModulePowerLevel(v.trim()));

            ParsingUtils.parseField(output, 0,
                LANE_RATIO_TYPE::matcher,
                m -> m.group("rat"),
                v -> extIdBuilder.setLaneRatioType(v.trim()));

            ParsingUtils.parseField(output, 0,
                WDM_TYPE::matcher,
                m -> m.group("wdm"),
                v -> extIdBuilder.setWdmType(v.trim()));

            ParsingUtils.parseField(output, 0,
                CLEI_CODE_PRESENT::matcher,
                m -> m.group("clei"),
                v -> extIdBuilder.setCleiCodePresent(isTrue(v)));

            if (extIdBuilder.isCleiCodePresent() != null || extIdBuilder.getExtIdentifier() != null
                    || extIdBuilder.getLaneRatioType() != null || extIdBuilder.getWdmType() != null
                    || extIdBuilder.getModulePowerLevel() != null) {
                builder.setExIdentifier(extIdBuilder.build());
            }

            ParsingUtils.parseField(output, 0,
                CONNECTOR::matcher,
                m -> m.group("conn"),
                v -> cienaBuilder.setConnector(v.trim()));

            ParsingUtils.parseField(output, 0,
                TRANSCEIVER_CODES::matcher,
                m -> m.group("tc"),
                v -> transceiverCodesBuilder.setTransceiverCodes(v.trim()));

            ParsingUtils.parseField(output, 0,
                ETHERNET_COMPLIANCE::matcher,
                m -> m.group("eth"),
                v -> transceiverCodesBuilder.setEthernetCompliance(v.trim()));

            ParsingUtils.parseField(output, 0,
                FIBER_COMPLIANCE::matcher,
                m -> m.group("fib"),
                v -> transceiverCodesBuilder.setFiberCompliance(v.trim()));

            ParsingUtils.parseField(output, 0,
                COPPER_COMPLIANCE::matcher,
                m -> m.group("cop"),
                v -> transceiverCodesBuilder.setCopperCompliance(v.trim()));

            ParsingUtils.parseField(output, 0,
                SONET_COMPLIANCE::matcher,
                m -> m.group("sonet"),
                v -> transceiverCodesBuilder.setSonetCompliance(v.trim()));

            ParsingUtils.parseField(output, 0,
                OTN_COMPLIANCE::matcher,
                m -> m.group("otn"),
                v -> transceiverCodesBuilder.setOtnCompliance(v.trim()));

            if (isTransceiverCodesNotEmpty(transceiverCodesBuilder)) {
                builder.setTransceiverCodesProps(transceiverCodesBuilder.build());
            }

            ParsingUtils.parseField(output, 0,
                RATES_SUPPORTED::matcher,
                m -> m.group("rs"),
                v -> ratesBuilder.setRatesSupported(v.trim()));

            ParsingUtils.parseField(output, 0,
                RATE_111::matcher,
                m -> m.group("rat"),
                v -> ratesBuilder.setRate1118(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                RATE_103::matcher,
                m -> m.group("rat"),
                v -> ratesBuilder.setRate103125(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                RATE_41::matcher,
                m -> m.group("rat"),
                v -> ratesBuilder.setRate4125(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                RATE_43::matcher,
                m -> m.group("rat"),
                v -> ratesBuilder.setRate43(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                RATE_39::matcher,
                m -> m.group("rat"),
                v -> ratesBuilder.setRate398(isTrue(v)));

            if (ratesBuilder.getRatesSupported() != null || ratesBuilder.isRate1118() != null
                    || ratesBuilder.isRate103125() != null || ratesBuilder.isRate4125() != null
                    || ratesBuilder.isRate43() != null || ratesBuilder.isRate398() != null) {
                builder.setRatesSupported(ratesBuilder.build());
            }

            ParsingUtils.parseField(output, 0,
                LANES_SUPPORTED::matcher,
                m -> m.group("lanes"),
                v -> supportedLanesBuilder.setNumLanesSupported(v.trim()));

            ParsingUtils.parseField(output, 0,
                NETWORK_LANES::matcher,
                m -> m.group("lanes"),
                v -> supportedLanesBuilder.setNumNetworkLanes(Integer.valueOf(v.trim())));

            ParsingUtils.parseField(output, 0,
                HOST_LANES::matcher,
                m -> m.group("lanes"),
                v -> supportedLanesBuilder.setNumHostLanes(Integer.valueOf(v.trim())));

            if (supportedLanesBuilder.getNumLanesSupported() != null
                    || supportedLanesBuilder.getNumNetworkLanes() != null
                    || supportedLanesBuilder.getNumHostLanes() != null) {
                builder.setNumLanesSupported(supportedLanesBuilder.build());
            }

            ParsingUtils.parseField(output, 0,
                MEDIA_PROPERTIES::matcher,
                m -> m.group("med"),
                v -> mediaPropBuilder.setMediaProperties(v.trim()));

            ParsingUtils.parseField(output, 0,
                MEDIA_TYPE::matcher,
                m -> m.group("medType"),
                v -> mediaPropBuilder.setMediaType(v.trim()));

            ParsingUtils.parseField(output, 0,
                DIRECTIONALITY::matcher,
                m -> m.group("dir"),
                v -> mediaPropBuilder.setDirectionality(v.trim()));

            ParsingUtils.parseField(output, 0,
                OPTICAL_MUX_DEMUX::matcher,
                m -> m.group("optMuxDemux"),
                v -> mediaPropBuilder.setOpticalMuxDemux(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                ACTIVE_FIBER_PER_CONN::matcher,
                m -> m.group("actFiber"),
                v -> mediaPropBuilder.setActiveFiberPerCon(v.trim()));

            if (mediaPropBuilder.getMediaProperties() != null || mediaPropBuilder.getMediaType() != null
                    || mediaPropBuilder.getDirectionality() != null || mediaPropBuilder.isOpticalMuxDemux() != null
                    || mediaPropBuilder.getActiveFiberPerCon() != null) {
                builder.setMediaProperties(mediaPropBuilder.build());
            }
            ParsingUtils.parseField(output, 0,
                MAX_NETWORK_LANE_BR::matcher,
                m -> m.group("maxLane"),
                v -> builder.setMaxNetworkLaneBr(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_HOST_LANE_BR::matcher,
                m -> m.group("maxLane"),
                v -> builder.setMaxHostLaneBr(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_SM_FIBER_LENGTH::matcher,
                m -> m.group("fibLength"),
                v -> builder.setMaxSmFiberLength(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_MM_FIBER_LENGTH::matcher,
                m -> m.group("fibLength"),
                v -> builder.setMaxMmFiberLength(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_CU_CABLE_LENGTH::matcher,
                m -> m.group("cabLength"),
                v -> builder.setMaxCuCableLength(v.trim()));

            ParsingUtils.parseField(output, 0,
                MIN_WAVELENGTH_PER_FIBER::matcher,
                m -> m.group("minWave"),
                v -> builder.setMinWavelengthPerFiber(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_WAVELENGTH_PER_FIBER::matcher,
                m -> m.group("maxWave"),
                v -> builder.setMaxWavelengthPerFiber(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_PER_LANE_OPT_WIDTH::matcher,
                m -> m.group("optWidth"),
                v -> builder.setMaxPerLaneOptWidth(v.trim()));

            ParsingUtils.parseField(output, 0,
                DEVICE_TECHNOLOGY_1::matcher,
                m -> m.group("dev"),
                v -> deviceTechBuilder.setDeviceTechnology1(v.trim()));

            ParsingUtils.parseField(output, 0,
                DEVICE_TECHNOLOGY_2::matcher,
                m -> m.group("dev"),
                v -> deviceTechBuilder.setDeviceTechnology2(v.trim()));

            ParsingUtils.parseField(output, 0,
                LASER_SOURCE_TECH::matcher,
                m -> m.group("laser"),
                v -> deviceTechBuilder.setLaserSourceTech(v.trim()));

            ParsingUtils.parseField(output, 0,
                TX_MODULATION_TECH::matcher,
                m -> m.group("txMod"),
                v -> deviceTechBuilder.setTxModulationTech(v.trim()));

            ParsingUtils.parseField(output, 0,
                WAVELENGTH_CONTROL::matcher,
                m -> m.group("waveControl"),
                v -> deviceTechBuilder.setWavelengthControl(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                COOLED_TR::matcher,
                m -> m.group("cooledTr"),
                v -> deviceTechBuilder.setCooledTransmitter(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                TUNABILITY::matcher,
                m -> m.group("tunability"),
                v -> deviceTechBuilder.setTunability(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                VOA_IMPL::matcher,
                m -> m.group("voa"),
                v -> deviceTechBuilder.setVoaImplemented(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                DETECTOR_TYPE::matcher,
                m -> m.group("det"),
                v -> deviceTechBuilder.setDetectorType(v.trim()));

            ParsingUtils.parseField(output, 0,
                CDR_EDC::matcher,
                m -> m.group("cdrEdc"),
                v -> deviceTechBuilder.setCdrWithEdc(isTrue(v)));

            if (isDeviceTechnologyNotEmpty(deviceTechBuilder)) {
                builder.setDeviceTechnology(deviceTechBuilder.build());
            }

            ParsingUtils.parseField(output, 0,
                SIG_CODE::matcher,
                m -> m.group("sigc"),
                v -> sigCodeBuilder.setSignalCode(v.trim()));

            ParsingUtils.parseField(output, 0,
                SIG_MODULATION::matcher,
                m -> m.group("modulation"),
                v -> sigCodeBuilder.setModulation(v.trim()));

            ParsingUtils.parseField(output, 0,
                SIG_CODING::matcher,
                m -> m.group("sigc"),
                v -> sigCodeBuilder.setSignalCoding(v.trim()));

            if (sigCodeBuilder.getModulation() != null || sigCodeBuilder.getSignalCoding() != null
                    || sigCodeBuilder.getSignalCode() != null) {
                builder.setSignalCode(sigCodeBuilder.build());
            }

            ParsingUtils.parseField(output, 0,
                MAX_OUTPUT_PWR_PER_CON::matcher,
                m -> m.group("pwr"),
                v -> builder.setMaxOutputPwrPerCon(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_INPUT_PWR_PER_LANE::matcher,
                m -> m.group("pwr"),
                v -> builder.setMaxInputPwrPerLane(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_PWR_CONSUMPTION::matcher,
                m -> m.group("pwr"),
                v -> builder.setMaxPwrConsumption(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_PWR_IN_LOW_PWR_MODE::matcher,
                m -> m.group("pwr"),
                v -> builder.setMaxPwrInLowPwrMode(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_OPER_CASE_TEMP::matcher,
                m -> m.group("temp"),
                v -> builder.setMaxOperCaseTemp(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_OPER_CASE_TEMP::matcher,
                m -> m.group("temp"),
                v -> builder.setMaxOperCaseTemp(v.trim()));

            ParsingUtils.parseField(output, 0,
                MIN_OPER_CASE_TEMP::matcher,
                m -> m.group("temp"),
                v -> builder.setMinOperCaseTemp(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_HIGH_POWER_UP_TIME::matcher,
                m -> m.group("time"),
                v -> builder.setMaxHighPowerUpTime(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_HIGH_POWER_DOWN_TIME::matcher,
                m -> m.group("time"),
                v -> builder.setMaxHighPowerDownTime(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_TX_TURN_ON_TIME::matcher,
                m -> m.group("time"),
                v -> builder.setMaxTxTurnOnTime(v.trim()));

            ParsingUtils.parseField(output, 0,
                MAX_TX_TURN_OFF_TIME::matcher,
                m -> m.group("time"),
                v -> builder.setMaxTxTurnOffTime(v.trim()));

            ParsingUtils.parseField(output, 0,
                HEAT_SINK_TYPE::matcher,
                m -> m.group("type"),
                v -> builder.setHeatSinkType(v.trim()));

            ParsingUtils.parseField(output, 0,
                HOST_LN_SIG_SPEC::matcher,
                m -> m.group("spec"),
                v -> builder.setHostLnSignalSpec(v.trim()));

            ParsingUtils.parseField(output, 0,
                CFP_MSA_HW_SPEC_REV::matcher,
                m -> m.group("cfp"),
                v -> builder.setCfpMsaHwSpecRev(v.trim()));

            ParsingUtils.parseField(output, 0,
                CFP_MSA_MGMT_IF_SPEC_REV::matcher,
                m -> m.group("cfp"),
                v -> builder.setCfpMsaMgmtIfSpecRev(v.trim()));

            ParsingUtils.parseField(output, 0,
                MODULE_HW_VER::matcher,
                m -> m.group("hwVer"),
                v -> builder.setModuleHwVersion(v.trim()));

            ParsingUtils.parseField(output, 0,
                MODULE_FW_VER::matcher,
                m -> m.group("fwVer"),
                v -> builder.setModuleFwVersion(v.trim()));

            ParsingUtils.parseField(output, 0,
                DIAG_MONITOR_TYPE::matcher,
                m -> m.group("type"),
                v -> diagMonitorTypeBuilder.setDiagMonitorType(v.trim()));

            ParsingUtils.parseField(output, 0,
                RX_PWR_MEAS_TYPE::matcher,
                m -> m.group("type"),
                v -> diagMonitorTypeBuilder.setRxPowerMeasType(v.trim()));

            ParsingUtils.parseField(output, 0,
                TX_PWR_MEAS_TYPE::matcher,
                m -> m.group("type"),
                v -> diagMonitorTypeBuilder.setTxPowerMeasType(v.trim()));

            if (diagMonitorTypeBuilder.getDiagMonitorType() != null
                    || diagMonitorTypeBuilder.getRxPowerMeasType() != null
                    || diagMonitorTypeBuilder.getTxPowerMeasType() != null) {
                builder.setDiagnosticMonitorType(diagMonitorTypeBuilder.build());
            }

            ParsingUtils.parseField(output, 0,
                DIAG_MONITOR_CAPS_1::matcher,
                m -> m.group("caps"),
                v -> diagMonitorCapsBuilder.setDiagMonitorCaps1(v.trim()));

            ParsingUtils.parseField(output, 0,
                DIAG_MONITOR_CAPS_2::matcher,
                m -> m.group("caps"),
                v -> diagMonitorCapsBuilder.setDiagMonitorCaps2(v.trim()));

            ParsingUtils.parseField(output, 0,
                TX_AUX_MONITOR_1::matcher,
                m -> m.group("aux"),
                v -> diagMonitorCapsBuilder.setTxAuxMonitor1(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                TX_AUX_MONITOR_2::matcher,
                m -> m.group("aux"),
                v -> diagMonitorCapsBuilder.setTxAuxMonitor2(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                TX_SOA_BIAS_CURRENT::matcher,
                m -> m.group("current"),
                v -> diagMonitorCapsBuilder.setTxSoaBiasCurrent(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                TX_PWR_SUPPLY_VOLTAGE::matcher,
                m -> m.group("voltage"),
                v -> diagMonitorCapsBuilder.setTxPwrSupplyVoltage(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                TX_TEMPERATURE::matcher,
                m -> m.group("temp"),
                v -> diagMonitorCapsBuilder.setTxTemperature(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                NET_LN_RX_PWR::matcher,
                m -> m.group("pwr"),
                v -> diagMonitorCapsBuilder.setNetworkLnRxPwr(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                NET_LN_OUT_PWR::matcher,
                m -> m.group("pwr"),
                v -> diagMonitorCapsBuilder.setNetworkLnOutputPwr(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                NET_LN_BIAS_CURRENT::matcher,
                m -> m.group("current"),
                v -> diagMonitorCapsBuilder.setNetworkLnBiasCurrent(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                NET_LN_TEMP::matcher,
                m -> m.group("temperature"),
                v -> diagMonitorCapsBuilder.setNetworkLnTemperature(isTrue(v)));

            if (isDiagMonitorCapsBuilderNotEmpty(diagMonitorCapsBuilder)) {
                builder.setDiagMonitorCaps(diagMonitorCapsBuilder.build());
            }

            ParsingUtils.parseField(output, 0,
                ENH_OPT_1::matcher,
                m -> m.group("opt"),
                v -> enhancedOptBuilder.setEnhancedOptions1(v.trim()));

            ParsingUtils.parseField(output, 0,
                ENH_OPT_2::matcher,
                m -> m.group("opt"),
                v -> enhancedOptBuilder.setEnhancedOptions2(v.trim()));

            ParsingUtils.parseField(output, 0,
                HOST_LN_LOOPBACK::matcher,
                m -> m.group("loopback"),
                v -> enhancedOptBuilder.setHostLnLoopback(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                HOST_LN_PRBS::matcher,
                m -> m.group("prbs"),
                v -> enhancedOptBuilder.setHostLnPrbs(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                HOST_LN_EMP_CTRL::matcher,
                m -> m.group("ctrl"),
                v -> enhancedOptBuilder.setHostLnEmphasisCtrl(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                NET_LN_LOOPBACK::matcher,
                m -> m.group("loopback"),
                v -> enhancedOptBuilder.setNetworkLnLoopback(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                NET_LN_PRBS::matcher,
                m -> m.group("prbs"),
                v -> enhancedOptBuilder.setNetworkLnPrbs(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                AMPL_ADJUST::matcher,
                m -> m.group("adjust"),
                v -> enhancedOptBuilder.setAmplitudeAdjustment(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                PHASE_ADJ::matcher,
                m -> m.group("adjust"),
                v -> enhancedOptBuilder.setPhaseAdjustment(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                UNIDIR_TX_RX::matcher,
                m -> m.group("unidir"),
                v -> enhancedOptBuilder.setUnidirectionalTxRx(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                ACT_VOLT_PHA_FUNCT::matcher,
                m -> m.group("function"),
                v -> enhancedOptBuilder.setActiveVoltPhaseFunc(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                RX_FIFO_RESET::matcher,
                m -> m.group("reset"),
                v -> enhancedOptBuilder.setRxFifoReset(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                RX_FIFO_AUTO_RESET::matcher,
                m -> m.group("reset"),
                v -> enhancedOptBuilder.setRxFifoAutoReset(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                TX_FIFO_RESET::matcher,
                m -> m.group("reset"),
                v -> enhancedOptBuilder.setTxFifoReset(isTrue(v)));

            ParsingUtils.parseField(output, 0,
                TX_FIFO_AUTO_RESET::matcher,
                m -> m.group("reset"),
                v -> enhancedOptBuilder.setTxFifoAutoReset(isTrue(v)));

            if (isEnhancedOptionsNotEmpty(enhancedOptBuilder)) {
                builder.setEnhancedOpt(enhancedOptBuilder.build());
            }

            ParsingUtils.parseField(oneLineOutput, 0,
                TX_RX_MONITOR::matcher,
                m -> m.group("txMonitor"),
                v -> txMonitorOptBuilder.setTxMonitorClockOptions(v.trim()));

            ParsingUtils.parseField(oneLineOutput, 0,
                TX_MONITOR_16_H::matcher,
                m -> m.group("hostLnRate16TX"),
                v -> txMonitorOptBuilder.setHostLnRate116(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                TX_MONITOR_16_N::matcher,
                m -> m.group("netLnRate16TX"),
                v -> txMonitorOptBuilder.setNetworkLnRate116(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                TX_MONITOR_64_H::matcher,
                m -> m.group("hostLnRate64TX"),
                v -> txMonitorOptBuilder.setHostLnRate164(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                TX_MONITOR_64_N::matcher,
                m -> m.group("netLnRate64TX"),
                v -> txMonitorOptBuilder.setNetworkLnRate164(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                TX_MONITOR_8::matcher,
                m -> m.group("netLnRate8TX"),
                v -> txMonitorOptBuilder.setNetworkLnRate18(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                TX_MONITOR_CLOCK::matcher,
                m -> m.group("monitorOptionTX"),
                v -> txMonitorOptBuilder.setMonitorClockOption(isTrue(v)));

            if (isTxMonitorClockOptionsNotEmpty(txMonitorOptBuilder)) {
                builder.setTxMonitorClockOptions(txMonitorOptBuilder.build());
            }

            ParsingUtils.parseField(oneLineOutput, 0,
                TX_RX_MONITOR::matcher,
                m -> m.group("rxMonitor"),
                v -> rxMonitorOptBuilder.setRxMonitorClockOptions(v.trim()));

            ParsingUtils.parseField(oneLineOutput, 0,
                RX_MONITOR_16_H::matcher,
                m -> m.group("hostLnRate16RX"),
                v -> rxMonitorOptBuilder.setHostLnRate116(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                RX_MONITOR_16_N::matcher,
                m -> m.group("netLnRate16RX"),
                v -> rxMonitorOptBuilder.setNetworkLnRate116(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                RX_MONITOR_64_H::matcher,
                m -> m.group("hostLnRate64RX"),
                v -> rxMonitorOptBuilder.setHostLnRate164(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                RX_MONITOR_64_N::matcher,
                m -> m.group("netLnRate64RX"),
                v -> rxMonitorOptBuilder.setNetworkLnRate164(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                RX_MONITOR_8::matcher,
                m -> m.group("netLnRate8RX"),
                v -> rxMonitorOptBuilder.setNetworkLnRate18(isTrue(v)));

            ParsingUtils.parseField(oneLineOutput, 0,
                RX_MONITOR_CLOCK::matcher,
                m -> m.group("monitorOptionRX"),
                v -> rxMonitorOptBuilder.setMonitorClockOption(isTrue(v)));

            if (isRxMonitorOptionsNotEMpty(rxMonitorOptBuilder)) {
                builder.setRxMonitorClockOptions(rxMonitorOptBuilder.build());
            }
        }
    }

    private static String processOutput(String output, boolean onlyNewLine) {
        var result = output.replaceAll("\\n", " ")
                .replaceAll("\\\\n", " ")
                .replaceAll("\\r", "");
        if (onlyNewLine) {
            return result;
        }
        return result.replaceAll("--\\+ * \\+--", "--+\n+--");
    }

    private static boolean isTrue(String testString) {
        return testString.trim().equals("Yes");
    }

    private static boolean isTransceiverCodesNotEmpty(final TransceiverCodesPropsBuilder builder) {
        return builder.getTransceiverCodes() != null || builder.getEthernetCompliance() != null
                || builder.getFiberCompliance() != null || builder.getCopperCompliance() != null
                || builder.getSonetCompliance() != null || builder.getOtnCompliance() != null;
    }

    private static boolean isDeviceTechnologyNotEmpty(final DeviceTechnologyBuilder builder) {
        return builder.getDeviceTechnology1() != null || builder.getDeviceTechnology2() != null
                || builder.getLaserSourceTech() != null || builder.getTxModulationTech() != null
                || builder.isWavelengthControl() != null || builder.isCooledTransmitter() != null
                || builder.isTunability() != null || builder.isVoaImplemented() != null
                || builder.getDetectorType() != null || builder.isCdrWithEdc() != null;
    }

    private static boolean isDiagMonitorCapsBuilderNotEmpty(final DiagMonitorCapsBuilder builder) {
        return builder.getDiagMonitorCaps1() != null || builder.getDiagMonitorCaps2() != null
                || builder.isTxAuxMonitor1() != null || builder.isTxAuxMonitor2() != null
                || builder.isTxSoaBiasCurrent() != null || builder.isTxPwrSupplyVoltage() != null
                || builder.isTxTemperature() != null || builder.isNetworkLnRxPwr() != null
                || builder.isNetworkLnOutputPwr() != null || builder.isNetworkLnBiasCurrent() != null
                || builder.isNetworkLnTemperature() != null;
    }

    private static boolean isEnhancedOptionsNotEmpty(final EnhancedOptBuilder builder) {
        return builder.getEnhancedOptions1() != null || builder.getEnhancedOptions2() != null
                || builder.isHostLnLoopback() != null || builder.isHostLnPrbs() != null
                || builder.isHostLnEmphasisCtrl() != null || builder.isNetworkLnLoopback() != null
                || builder.isNetworkLnPrbs() != null || builder.isAmplitudeAdjustment() != null
                || builder.isPhaseAdjustment() != null || builder.isUnidirectionalTxRx() != null
                || builder.isActiveVoltPhaseFunc() != null || builder.isRxFifoReset() != null
                || builder.isRxFifoAutoReset() != null || builder.isTxFifoReset() != null
                || builder.isTxFifoAutoReset() != null;
    }

    private static boolean isTxMonitorClockOptionsNotEmpty(final TxMonitorClockOptionsBuilder builder) {
        return builder.getTxMonitorClockOptions() != null || builder.isHostLnRate116() != null
                || builder.isNetworkLnRate116() != null || builder.isHostLnRate164() != null
                || builder.isNetworkLnRate164() != null || builder.isNetworkLnRate18() != null
                || builder.isMonitorClockOption() != null;
    }

    private static boolean isRxMonitorOptionsNotEMpty(final RxMonitorClockOptionsBuilder builder) {
        return builder.getRxMonitorClockOptions() != null || builder.isHostLnRate116() != null
                || builder.isNetworkLnRate116() != null || builder.isHostLnRate164() != null
                || builder.isNetworkLnRate164() != null || builder.isNetworkLnRate18() != null
                || builder.isMonitorClockOption() != null;
    }
}