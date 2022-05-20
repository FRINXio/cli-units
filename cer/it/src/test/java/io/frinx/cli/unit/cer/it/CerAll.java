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
package io.frinx.cli.unit.cer.it;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
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
import io.frinx.cli.io.impl.CliDefaultParametersService;
import io.frinx.cli.io.impl.IOConfigurationBuilder;
import io.frinx.cli.io.impl.cli.KeepaliveCli;
import io.frinx.cli.registry.api.TranslateContext;
import io.frinx.cli.registry.impl.TranslateRegistryImpl;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.cer.ifc.CerInterfaceUnit;
import io.frinx.cli.unit.cer.init.CerCliInitializerUnit;
import io.frinx.cli.unit.generic.GenericTranslateUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.handler.spi.GenericTranslateContext;
import io.frinx.translate.unit.commons.utils.NoopDataBroker;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.security.Security;
import java.util.Collections;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMAdapterLoader;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMService;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.util.DataReader;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeConnectionParameters;
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
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.LoggerFactory;

public class CerAll {

    private static final Device CER_ALL = new DeviceIdBuilder()
            .setDeviceType("cer")
            .setDeviceVersion("*")
            .build();

    private static final String MOUNT_ID = "cer-it";
    private static final int PORT = 51022;

    private static final String HOST = "127.0.0.1";
    private static final InetSocketAddress CER_ADDR = new InetSocketAddress(HOST, PORT);
    private static final CliNode CLI_CFG = new CliNodeBuilder()
            .setPort(new PortNumber(PORT))
            .setHost(new Host(new IpAddress(new Ipv4Address(HOST))))
            .setDeviceType(CER_ALL.getDeviceType())
            .setDeviceVersion(CER_ALL.getDeviceVersion())
            .setTransportType(CliNodeConnectionParameters.TransportType.Ssh)
            .setCredentials(new LoginPasswordBuilder()
                    .setUsername("frinx")
                    .setPassword("G1DwrEmBghEvsMA65bqf")
                    .build())
            .setKeepaliveStrategy(new KeepaliveBuilder()
                    .setKeepaliveDelay(30)
                    .setKeepaliveTimeout(30)
                    .setKeepaliveInitialDelay(30)
                    .build())
            .setMaxConnectionAttempts(1L)
            .setMaxReconnectionAttempts(1L)
            .build();

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(4);

    private static final KeepaliveCli.ReconnectListener RECONNECT_LISTENER = new KeepaliveCli.ReconnectListener() {
        @Override
        public void onDisconnected(@Nonnull CompletionStage<? extends Cli> init, Throwable throwable) {
            throw new RuntimeException("Disconnected: " + throwable);
        }

        @Override
        public void onReconnecting(Throwable throwable, long reconnectCounter) {
            throw new RuntimeException("Reconnecting " + reconnectCounter + " : " + throwable);
        }

        @Override
        public void onFailedConnection(final String errorMessage) {
            throw new RuntimeException("Connection failed: " + errorMessage);
        }

        @Override
        public void onStatusMsgUpdate(String newStatus) {
            // NOOP
        }
    };

    public static final TopologyKey CLI_TOPO_KEY = new TopologyKey(new TopologyId("cli"));

    @Mock
    private DataBroker mockBroker;
    @Mock
    private ReadWriteTransaction mockTx;
    @Mock
    private BindingTransactionChain mockTxChain;
    @Mock
    private CliDefaultParametersService cliDefaultParametersService;

    private DataBroker bindingBroker;
    protected RpcRegistry rpcReg;
    private DOMDataBroker domBroker;
    private SchemaContext schemaCtx;
    private Cli cli;

    protected boolean failFast = true;

    private static Object invoke(Object object, Method method, Object[] objects) {
        return null;
    }

    @Before
    public void setUp() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
//        setRootLogLevel();
        MockitoAnnotations.initMocks(this);
        mockBroker();
        mockDefaultParamsService();

        TranslateRegistryImpl reg = getTranslateRegistry(mockBroker);

        TranslateContext translateContext = reg.getTranslateContext(getDeviceId());

        RemoteDeviceId remoteId = new RemoteDeviceId(CLI_TOPO_KEY, MOUNT_ID, getAddress());

        IOConfigurationBuilder ioConfigurationBuilder = new IOConfigurationBuilder()
                .setId(remoteId)
                .setCliFlavour(translateContext.getCliFlavour())
                .setCliConfiguration(getCliNode())
                .setInitializer(translateContext.getInitializer(remoteId, getCliNode()))
                .setPromptResolver(translateContext.getPromptResolver())
                .setKeepaliveExecutor(EXECUTOR)
                .setCliInitExecutor(ForkJoinPool.commonPool())
                .setReconnectListener(RECONNECT_LISTENER)
                .setErrorPatterns(translateContext.getErrorPatterns())
                .setCliDefaultParametersService(cliDefaultParametersService);

        cli = ioConfigurationBuilder.getIO()
                .toCompletableFuture()
                .get();

        schemaCtx = translateContext.getSchemaContext();
        BindingToNormalizedNodeCodec codec = translateContext.getCodec();

        // Get & register CRUD handlers
        CustomizerReadRegistryBuilder readerRegistryBuilder = new CustomizerReadRegistryBuilder(new YangDAG());
        CustomizerWriterRegistryBuilder writerRegistryBuilder = new CustomizerWriterRegistryBuilder(new YangDAG());
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

    private void mockDefaultParamsService() {
        Mockito.doReturn(null).when(cliDefaultParametersService).readDefaultParameters();
        Mockito.doReturn(CLI_CFG.getKeepaliveStrategy()).when(cliDefaultParametersService).getKeepaliveStrategy(Mockito.any(), Mockito.any());
        Mockito.doReturn(CLI_CFG.getDryRunJournalSize()).when(cliDefaultParametersService).getDryrunJournalSize(Mockito.any(), Mockito.any());
        Mockito.doReturn(CLI_CFG.getJournalSize()).when(cliDefaultParametersService).getJournalSize(Mockito.any(), Mockito.any());
        Mockito.doReturn(CLI_CFG.getJournalLevel()).when(cliDefaultParametersService).getJournalLevel(Mockito.any(), Mockito.any());
        Mockito.doReturn(CLI_CFG.getParsingEngine()).when(cliDefaultParametersService).getParsingEngine(Mockito.any(), Mockito.any());
        Mockito.doReturn(CLI_CFG.getMaxConnectionAttempts()).when(cliDefaultParametersService)
                .getMaxConnectionAttempts(Mockito.any(), Mockito.any());
        Mockito.doReturn(CLI_CFG.getMaxReconnectionAttempts()).when(cliDefaultParametersService)
                .getMaxReconnectionAttempts(Mockito.any(), Mockito.any());
    }

    protected InetSocketAddress getAddress() {
        return CER_ADDR;
    }

    protected Device getDeviceId() {
        return CER_ALL;
    }

    private void setRootLogLevel() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        Logger localCli = (Logger) LoggerFactory.getLogger("io.frinx.cli");
        localCli.setLevel(Level.DEBUG);
//        localCli = (Logger) LoggerFactory.getLogger("org.apache.sshd");
//        localCli.setLevel(Level.TRACE);
    }

    private void mockBroker() {
        Mockito.doReturn(Futures.immediateCheckedFuture(null)).when(mockTx).submit();
        Mockito.doReturn(Mockito.mock(ReadOnlyTransaction.class)).when(mockBroker).newReadOnlyTransaction();
        Mockito.doReturn(mockTx).when(mockBroker).newReadWriteTransaction();
        Mockito.doReturn(mockTx).when(mockBroker).newWriteOnlyTransaction();
        Mockito.doReturn(mockTxChain).when(mockBroker)
                .createTransactionChain(Mockito.any(TransactionChainListener.class));
        Mockito.doReturn(Mockito.mock(ReadOnlyTransaction.class)).when(mockTxChain).newReadOnlyTransaction();
        Mockito.doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();
        Mockito.doReturn(mockTx).when(mockTxChain).newWriteOnlyTransaction();

    }

    protected TranslateRegistryImpl getTranslateRegistry(DataBroker broker) {
        TranslateRegistryImpl reg = new TranslateRegistryImpl(broker);

        new GenericTranslateUnit(reg).init();
        new CerCliInitializerUnit(reg).init();
        new CerInterfaceUnit(reg).init();

        return reg;
    }

    private static DataTree getDataTree(final SchemaContext ctx, final DataTreeConfiguration configuration) {
        final DataTree dataTree = new InMemoryDataTreeFactory().create(configuration);
        dataTree.setSchemaContext(ctx);
        return dataTree;
    }

    private static DOMDataBroker getDomBroker(SchemaContext schemaContext,
                                              DataBroker contextBroker,
                                              BindingNormalizedNodeSerializer serializer,
                                              ReaderRegistry readerRegistry,
                                              WriterRegistry writerRegistry,
                                              DataTree dataTree) {
        ReadableDataManager.Typed readTree = new ReadableDataTreeDelegator(
                serializer, schemaContext, readerRegistry, contextBroker);

        ModifiableDirectDataTreeDelegator modTree = new ModifiableDirectDataTreeDelegator(
                serializer, dataTree, schemaContext, writerRegistry, contextBroker, readTree,
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

    @Ignore
    @Test
    public void testConnectivity() throws Exception {
        cli.close();

        TranslateRegistryImpl reg = getTranslateRegistry(mockBroker);
        TranslateContext translateContext = reg.getTranslateContext(getDeviceId());

        RemoteDeviceId remoteId = new RemoteDeviceId(CLI_TOPO_KEY, MOUNT_ID, getAddress());

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

    protected CliNode getCliNode() {
        return (CliNode) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{CliNode.class},
            new CliNodeInvocationHandler(CLI_CFG));
    }

    @Ignore
    @Test
    public void getInterfacesBA() throws Exception {
        ReadOnlyTransaction readOnlyTransaction = bindingBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Interfaces>, ReadFailedException> read =
                readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, IIDs.INTERFACES);
        Interfaces interfaces = read.checkedGet().get();

    }

    /**
     * Get all data from the device. Can serve for basic testing but also for performance tuning.
     */
    @Ignore
    @Test
    public void getAllDOM() throws Exception {
        DOMDataReadOnlyTransaction readOnlyTransaction = domBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read =
                readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.EMPTY);
        NormalizedNode<?, ?> root = read.checkedGet().get();
    }

    @Ignore
    @SuppressWarnings("IllegalCatch")
    @Test
    public void getAllDOMBenchmark() throws Exception {
        benchmark(2, 10, () -> {
            try {
                getAllDOM();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    static void benchmark(int warmups, int rounds, Runnable code) {
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

    protected String toJson(NormalizedNode<?, ?> root) throws Exception {
        JSONCodecFactory codecFac = JSONCodecFactory.getShared(schemaCtx);
        StringWriter out = new StringWriter();

        try (JsonWriter jsonWriter = new JsonWriter(out)) {
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            NormalizedNodeWriter nnWriter = NormalizedNodeWriter.forStreamWriter(
                    JSONNormalizedNodeStreamWriter.createNestedWriter(codecFac, SchemaPath.ROOT, null, jsonWriter),
                    true);


            for (final NormalizedNode<?, ?> child : ((ContainerNode) root).getValue()) {
                nnWriter.write(child);
                nnWriter.flush();
            }
            jsonWriter.endObject();
            return out.toString();
        }
    }

    private class CliNodeInvocationHandler implements InvocationHandler, DataReader {
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
        public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
            return method.invoke(cliCfg, objects);
        }

        @Override
        public NormalizedNodeContainer<?, YangInstanceIdentifier.PathArgument, NormalizedNode<?, ?>> getData() {
            // Use codec to provide
            return (NormalizedNodeContainer<?, YangInstanceIdentifier.PathArgument, NormalizedNode<?, ?>>)
                    codec.toNormalizedNode(
                            InstanceIdentifier.builder(NetworkTopology.class)
                                    .child(Topology.class, CLI_TOPO_KEY)
                                    .child(Node.class, new NodeKey(new NodeId(MOUNT_ID)))
                                    .augmentation(CliNode.class).build(),
                            cliCfg).getValue();
        }
    }
}
