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

package io.frinx.cli.unit.saos.it;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonWriter;
import io.fd.honeycomb.data.ReadableDataManager;
import io.fd.honeycomb.data.impl.ModifiableDirectDataTreeDelegator;
import io.fd.honeycomb.data.impl.ReadableDataTreeDelegator;
import io.fd.honeycomb.rpc.RpcRegistry;
import io.fd.honeycomb.rpc.RpcRegistryBuilder;
import io.fd.honeycomb.translate.impl.read.registry.CustomizerReadRegistryBuilder;
import io.fd.honeycomb.translate.impl.read.registry.CustomizerWriterRegistryBuilder;
import io.fd.honeycomb.translate.read.registry.ReaderRegistry;
import io.fd.honeycomb.translate.util.YangDAG;
import io.fd.honeycomb.translate.write.registry.WriterRegistry;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.CliFlavour;
import io.frinx.cli.io.impl.CliDefaultParametersService;
import io.frinx.cli.io.impl.IOConfigurationBuilder;
import io.frinx.cli.io.impl.cli.CliLoggingBroker;
import io.frinx.cli.io.impl.cli.KeepaliveCli;
import io.frinx.cli.registry.api.TranslateContext;
import io.frinx.cli.registry.impl.TranslateRegistryImpl;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.generic.GenericTranslateUnit;
import io.frinx.cli.unit.saos.acl.AclUnit;
import io.frinx.cli.unit.saos.broadcast.containment.SaosBroadcastContainmentUnit;
import io.frinx.cli.unit.saos.conf.ConfigurationUnit;
import io.frinx.cli.unit.saos.init.SaosCliInitializerUnit;
import io.frinx.cli.unit.saos.l2.cft.SaosL2CftUnit;
import io.frinx.cli.unit.saos.logical.ring.SaosLogicalRingUnit;
import io.frinx.cli.unit.saos.network.instance.SaosNetworkInstanceUnit;
import io.frinx.cli.unit.saos.qos.SaosQosUnit;
import io.frinx.cli.unit.saos8.ifc.SaosInterfaceUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.handler.spi.GenericTranslateContext;
import io.frinx.translate.unit.commons.utils.NoopDataBroker;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.security.Security;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.logging.controller.api.LoggingController;
import org.opendaylight.controller.logging.controller.impl.LoggingControllerImpl;
import org.opendaylight.controller.logging.controller.impl.util.DataModelUtil;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMAdapterLoader;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.util.DataReader;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeConnectionParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeParsingParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.keepalive.keepalive.strategy.KeepaliveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonStreamWriterBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.LoggerFactory;

public class SaosAll {

    private static final Device SAOS = new DeviceIdBuilder()
            .setDeviceType("saos")
            .setDeviceVersion("8")
            .build();

    private static final String MOUNT_ID = "saos-it";
    private static final int PORT = 1024;
    private static final String HOST = "127.0.0.1";
    private static final InetSocketAddress ADDR = new InetSocketAddress(HOST, PORT);

    private static final CliNode CLI_CFG = new CliNodeBuilder()
            .setPort(new PortNumber(PORT))
            .setHost(new Host(new IpAddress(new Ipv4Address(HOST))))
            .setDeviceType(SAOS.getDeviceType())
            .setDeviceVersion(SAOS.getDeviceVersion())
            .setTransportType(CliNodeConnectionParameters.TransportType.Ssh)
            .setCredentials(new LoginPasswordBuilder()
                    .setUsername("ciena")
                    .setPassword("ciena")
                    .build())
            .setKeepaliveStrategy(new KeepaliveBuilder()
                    .setKeepaliveDelay(3600)
                    .setKeepaliveTimeout(3600)
                    .setKeepaliveInitialDelay(30)
                    .build())
            .setMaxConnectionAttempts(1L)
            .setMaxReconnectionAttempts(1L)
            .setParsingEngine(CliNodeParsingParameters.ParsingEngine.OneLineParser)
            .build();

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(4);

    private static final KeepaliveCli.ReconnectListener RECONNECT_LISTENER = new KeepaliveCli.ReconnectListener() {
        @Override
        public void onDisconnected(@NotNull CompletionStage<? extends Cli> init, Throwable throwable) {
            throw new RuntimeException("Disconnected: " + throwable);
        }

        @Override
        public void onReconnecting(Throwable throwable, long reconnectCounter) {
            throw new RuntimeException("Reconnecting " + reconnectCounter + " : " + throwable);
        }

        @Override
        public void onFailedConnection(final Throwable throwable) {
            throw new RuntimeException("Connection failed: " + throwable);
        }

        @Override
        public void onStatusMsgUpdate(String newStatus) {
            // NOOP
        }
    };

    private static final CliFlavour CLI_FLAVOUR = new CliFlavour(
            Pattern.compile("configuration (search string|show brief)"),
            "",
            "\"",
            "\"",
            null,
            null,
            null,
            ImmutableList.of("configuration save", "configuration show brief"),
            "",
            Cli.NEWLINE,
            "",
            List.of("^", "*"),
            "configuration show brief");

    public static final TopologyKey CLI_TOPO_KEY = new TopologyKey(new TopologyId("cli"));

    @Mock
    private DataBroker mockBroker;
    @Mock
    private ReadWriteTransaction mockTx;
    @Mock
    private BindingTransactionChain mockTxChain;
    @Mock
    private CliDefaultParametersService cliDefaultParametersService;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private RpcProviderRegistry rpcProviderRegistry;

    private DataBroker bindingBroker;
    private RpcRegistry rpcReg;
    private DOMDataBroker domBroker;
    private SchemaContext schemaCtx;
    private Cli cli;

    private boolean failFast = true;

    @BeforeEach
    void setUp() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        setRootLogLevel();
        MockitoAnnotations.initMocks(this);
        mockBroker();
        mockDefaultParamsService();
        TranslateRegistryImpl reg = getTranslateRegistry(mockBroker);
        TranslateContext translateContext = reg.getTranslateContext(SAOS);
        RemoteDeviceId remoteId = new RemoteDeviceId(CLI_TOPO_KEY, MOUNT_ID, ADDR);
        LoggingController loggingController = new LoggingControllerImpl(dataBroker, rpcProviderRegistry,
                DataModelUtil.createLoggingStatus(Collections.emptyList(), Collections.emptyList()));

        IOConfigurationBuilder ioConfigurationBuilder = new IOConfigurationBuilder()
                .setId(remoteId)
                .setCliFlavour(CLI_FLAVOUR)
                .setCliConfiguration(getCliNode())
                .setInitializer(translateContext.getInitializer(remoteId, getCliNode()))
                .setPromptResolver(translateContext.getPromptResolver())
                .setKeepaliveExecutor(EXECUTOR)
                .setCliInitExecutor(ForkJoinPool.commonPool())
                .setReconnectListener(RECONNECT_LISTENER)
                .setErrorPatterns(translateContext.getErrorPatterns())
                .setCliDefaultParametersService(cliDefaultParametersService)
                .setLoggingBroker(new CliLoggingBroker(loggingController));

        cli = ioConfigurationBuilder.getIO()
                .toCompletableFuture()
                .get();

        schemaCtx = translateContext.getSchemaContext();
        BindingToNormalizedNodeCodec codec = translateContext.getCodec();

        // Get & register CRUD handlers
        CustomizerReadRegistryBuilder readerRegistryBuilder = new CustomizerReadRegistryBuilder(new YangDAG<>());
        CustomizerWriterRegistryBuilder writerRegistryBuilder = new CustomizerWriterRegistryBuilder(new YangDAG<>());
        TranslateUnit.Context transportContext = () -> cli;

        translateContext.provideHandlers(readerRegistryBuilder, writerRegistryBuilder, transportContext);
        readerRegistryBuilder.setFailFast(failFast);
        ReaderRegistry readerRegistry = readerRegistryBuilder.build();
        WriterRegistry writerRegistry = writerRegistryBuilder.build();

        // Get DOM brokers
        final DataTree dataTree = getDataTree(schemaCtx, DataTreeConfiguration.DEFAULT_CONFIGURATION);
        domBroker = getDomBroker(schemaCtx, NoopDataBroker.NOOP_DATA_BROKER, codec, readerRegistry, writerRegistry,
                dataTree);

        // Get & register RPC handlers
        RpcRegistryBuilder rpcRegBuilder = new RpcRegistryBuilder();
        translateContext.getRpcs(transportContext).forEach(rpcRegBuilder::addService);
        rpcReg = rpcRegBuilder.build();

        bindingBroker = ((DataBroker) new BindingDOMAdapterLoader(codec) {
            protected DOMService getDelegate(Class<? extends DOMService> reqDeleg) {
                return domBroker;
            }
        }.load(DataBroker.class).get());
    }

    private static DataTree getDataTree(final SchemaContext ctx, final DataTreeConfiguration configuration) {
        final DataTree dataTree = new InMemoryDataTreeFactory().create(configuration);
        dataTree.setSchemaContext(ctx);
        return dataTree;
    }

    private void mockDefaultParamsService() {
        Mockito.doReturn(CLI_CFG.getKeepaliveStrategy()).when(cliDefaultParametersService)
                .getKeepaliveStrategy(Mockito.any());
        Mockito.doReturn(CLI_CFG.getDryRunJournalSize()).when(cliDefaultParametersService)
                .getDryrunJournalSize(Mockito.any());
        Mockito.doReturn(CLI_CFG.getJournalSize()).when(cliDefaultParametersService).getJournalSize(Mockito.any());
        Mockito.doReturn(CLI_CFG.getJournalLevel()).when(cliDefaultParametersService).getJournalLevel(Mockito.any());
        Mockito.doReturn(CLI_CFG.getParsingEngine()).when(cliDefaultParametersService).getParsingEngine(Mockito.any());
        Mockito.doReturn(CLI_CFG.getMaxConnectionAttempts()).when(cliDefaultParametersService)
                .getMaxConnectionAttempts(Mockito.any());
        Mockito.doReturn(CLI_CFG.getMaxReconnectionAttempts()).when(cliDefaultParametersService)
                .getMaxReconnectionAttempts(Mockito.any());
    }

    private static void setRootLogLevel() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        Logger localCli = (Logger) LoggerFactory.getLogger("io.frinx.cli");
        localCli.setLevel(Level.DEBUG);
//        localCli = (Logger) LoggerFactory.getLogger("org.apache.sshd");
//        localCli.setLevel(Level.TRACE);
    }

    private void mockBroker() {
        Mockito.doReturn(CompletableFuture.completedFuture(null)).when(mockTx).submit();
        Mockito.doReturn(Mockito.mock(ReadOnlyTransaction.class)).when(mockBroker).newReadOnlyTransaction();
        Mockito.doReturn(mockTx).when(mockBroker).newReadWriteTransaction();
        Mockito.doReturn(mockTx).when(mockBroker).newWriteOnlyTransaction();
        Mockito.doReturn(mockTxChain).when(mockBroker)
                .createTransactionChain(Mockito.any(TransactionChainListener.class));
        Mockito.doReturn(Mockito.mock(ReadOnlyTransaction.class)).when(mockTxChain).newReadOnlyTransaction();
        Mockito.doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();
        Mockito.doReturn(mockTx).when(mockTxChain).newWriteOnlyTransaction();
    }

    private TranslateRegistryImpl getTranslateRegistry(DataBroker broker) {
        TranslateRegistryImpl reg = new TranslateRegistryImpl(broker);

        new GenericTranslateUnit(reg).init();
        new SaosCliInitializerUnit(reg).init();
        new ConfigurationUnit(reg).init();
        new SaosInterfaceUnit(reg).init();
        new io.frinx.cli.unit.saos.ifc.SaosInterfaceUnit(reg).init();
        new AclUnit(reg).init();
        new SaosQosUnit(reg).init();
        new SaosBroadcastContainmentUnit(reg).init();
        new SaosNetworkInstanceUnit(reg).init();
        new io.frinx.cli.unit.saos8.network.instance.SaosNetworkInstanceUnit(reg).init();
        new SaosLogicalRingUnit(reg).init();
        new SaosL2CftUnit(reg).init();

        return reg;
    }

    private static DOMDataBroker getDomBroker(SchemaContext schemaContext,
                                              DataBroker contextBroker,
                                              BindingNormalizedNodeSerializer serializer,
                                              ReaderRegistry readerRegistry,
                                              WriterRegistry writerRegistry,
                                              DataTree dataTree) {
        ReadableDataManager.Typed readTree = new ReadableDataTreeDelegator(serializer, schemaContext, readerRegistry,
                contextBroker);
        ModifiableDirectDataTreeDelegator modTree = new ModifiableDirectDataTreeDelegator(serializer, dataTree,
                schemaContext, writerRegistry, contextBroker, readTree,
                () -> {
                },
                () -> {
                },
                () -> {
                },
                e -> {
                });

        return new io.fd.honeycomb.data.impl.DataBroker(
                new io.fd.honeycomb.data.impl.DataBroker.MainPipelineTxFactory(modTree, readTree));
    }

    private CliNode getCliNode() {
        return (CliNode) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{CliNode.class}, new CliNodeInvocationHandler(CLI_CFG));
    }

    private static void benchmark(int warmups, int rounds, Runnable code) {
        long averageTime = 0;

        for (int i = 0; i < warmups; i++) {
            code.run();
        }

        for (int i = 0; i < rounds; i++) {
            Stopwatch stopwatch = Stopwatch.createStarted();

            code.run();

            stopwatch.stop();
            averageTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);
        }

    }

    private String toJson(NormalizedNode<?, ?> root) throws Exception {
        JSONCodecFactory codecFac = JSONCodecFactory.getShared(schemaCtx);
        StringWriter out = new StringWriter();

        try (JsonWriter jsonWriter = new JsonWriter(out)) {
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            NormalizedNodeWriter nnWriter = NormalizedNodeWriter.forStreamWriter(
                    new JsonStreamWriterBuilder()
                            .parentSchemaPath(SchemaPath.ROOT)
                            .codecFactory(codecFac)
                            .jsonWriter(jsonWriter)
                            .nestedWriter(true)
                            .createWriter(),
                    true);


            for (final NormalizedNode<?, ?> child : ((ContainerNode) root).getValue()) {
                nnWriter.write(child);
                nnWriter.flush();
            }
            jsonWriter.endObject();
            return out.toString();
        }
    }

    private class CliNodeInvocationHandler implements java.lang.reflect.InvocationHandler, DataReader {
        private final BindingToNormalizedNodeCodec codec;
        private CliNode cliCfg;

        CliNodeInvocationHandler(CliNode cliCfg) {
            this.cliCfg = cliCfg;
            ModuleInfoBackedContext mibContext = ModuleInfoBackedContext.create();
            mibContext.addModuleInfos(BindingReflections.loadModuleInfos());
            SchemaContext newCtx = mibContext.getSchemaContext();
            codec = GenericTranslateContext.getCodec(mibContext, newCtx);
        }

        @Override
        public Object invoke(Object object, java.lang.reflect.Method method, Object[] objects) throws Throwable {
            return method.invoke(cliCfg, objects);
        }

        @Override
        public NormalizedNodeContainer<?, YangInstanceIdentifier.PathArgument, NormalizedNode<?, ?>> getData() {
            // Use codec to provide normalizedNode version of cliCfg
            // This is for the cli default params provider service, it needs normalizedNode version of it
            return (NormalizedNodeContainer<?, YangInstanceIdentifier.PathArgument, NormalizedNode<?, ?>>)
                    codec.toNormalizedNode(
                            InstanceIdentifier.builder(NetworkTopology.class)
                                    .child(Topology.class, CLI_TOPO_KEY)
                                    .child(Node.class, new NodeKey(new NodeId(MOUNT_ID)))
                                    .augmentation(CliNode.class).build(),
                            cliCfg).getValue();
        }
    }

    @Disabled
    @Test
    void testConnectivity() throws Exception {
        cli.close();

        TranslateRegistryImpl reg = getTranslateRegistry(mockBroker);
        TranslateContext translateContext = reg.getTranslateContext(SAOS);

        RemoteDeviceId remoteId = new RemoteDeviceId(CLI_TOPO_KEY, MOUNT_ID, ADDR);

        for (int i = 0; i < 20; i++) {

            IOConfigurationBuilder ioConfigurationBuilder = new IOConfigurationBuilder()
                    .setId(remoteId)
                    .setCliConfiguration(getCliNode())
                    .setCliFlavour(translateContext.getCliFlavour())
                    .setInitializer(translateContext.getInitializer(remoteId, getCliNode()))
                    .setKeepaliveExecutor(EXECUTOR)
                    .setCliInitExecutor(ForkJoinPool.commonPool())
                    .setReconnectListener(RECONNECT_LISTENER)
                    .setPromptResolver(translateContext.getPromptResolver())
                    .setErrorPatterns(Collections.emptySet())
                    .setCliDefaultParametersService(cliDefaultParametersService);

            Cli io = ioConfigurationBuilder.getIO()
                    .toCompletableFuture()
                    .get();

            io.close();
        }
    }

    @Disabled
    @Test
    void getInterfacesBA() throws Exception {
        var readOnlyTransaction = bindingBroker.newReadOnlyTransaction();
        var read = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, IIDs.INTERFACES);
        var interfaces = read.get().get();
    }

    @Disabled
    @Test
    void getAllDOM() throws Exception {
        var readOnlyTransaction = domBroker.newReadOnlyTransaction();
        var read = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        var config = read.get().get();
    }

    @Disabled
    @SuppressWarnings("IllegalCatch")
    @Test
    void getAllDOMBenchmark() {
        benchmark(2, 10, () -> {
            try {
                getAllDOM();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}