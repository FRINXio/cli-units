/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.cubro.it;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.base.Stopwatch;
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
import io.frinx.cli.io.impl.IOConfigurationBuilder;
import io.frinx.cli.io.impl.cli.KeepaliveCli;
import io.frinx.cli.registry.api.TranslateContext;
import io.frinx.cli.registry.impl.TranslateRegistryImpl;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.cubro.conf.ConfigurationUnit;
import io.frinx.cli.unit.cubro.ifc.CubroInterfaceUnit;
import io.frinx.cli.unit.cubro.init.CubroCliInitializerUnit;
import io.frinx.cli.unit.cubro.unit.acl.AclUnit;
import io.frinx.cli.unit.generic.GenericTranslateUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.utils.NoopDataBroker;
import java.net.InetSocketAddress;
import java.security.Security;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMService;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
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
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.LoggerFactory;

public class CubroAll {

    public static final Device CUBRO_ALL = new DeviceIdBuilder()
            .setDeviceType("cubro")
            .setDeviceVersion("*")
            .build();

    private static final String MOUNT_ID = "cubro-it";
    private static final int PORT = 56800;

    private static final String HOST = "213.143.110.252";
    private static final InetSocketAddress CUBRO_ADDR = new InetSocketAddress(HOST, PORT);
    private static final CliNode CLI_CFG = new CliNodeBuilder()
            .setPort(new PortNumber(PORT))
            .setHost(new Host(new IpAddress(new Ipv4Address(HOST))))
            .setDeviceType(CUBRO_ALL.getDeviceType())
            .setDeviceVersion(CUBRO_ALL.getDeviceVersion())
            .setTransportType(CliNodeConnectionParameters.TransportType.Ssh)
            .setCredentials(new LoginPasswordBuilder()
                    .setUsername("root")
                    .setPassword("burningfirebeanplant")
                    .build())
            .setKeepaliveStrategy(new KeepaliveBuilder()
                    .setKeepaliveDelay(30)
                    .setKeepaliveTimeout(30)
                    .setKeepaliveInitialDelay(30)
                    .build())
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

    public static final TopologyKey CLI_TOPO_KEY = new TopologyKey(new TopologyId("cli"));

    @Mock
    private DataBroker mockBroker;
    @Mock
    private ReadWriteTransaction mockTx;
    @Mock
    private BindingTransactionChain mockTxChain;

    private DataBroker bindingBroker;
    protected RpcRegistry rpcReg;
    private DOMDataBroker domBroker;
    private SchemaContext schemaCtx;
    private Cli cli;

    protected boolean failFast = true;

    @BeforeEach
    void setUp() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        setRootLogLevel();
        MockitoAnnotations.initMocks(this);
        mockBroker();

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
                .setErrorPatterns(translateContext.getErrorPatterns());

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

    protected InetSocketAddress getAddress() {
        return CUBRO_ADDR;
    }

    protected Device getDeviceId() {
        return CUBRO_ALL;
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

    protected TranslateRegistryImpl getTranslateRegistry(DataBroker broker) {
        TranslateRegistryImpl reg = new TranslateRegistryImpl(broker);

        new GenericTranslateUnit(reg).init();
        new CubroCliInitializerUnit(reg).init();
        new ConfigurationUnit(reg).init();
        new CubroInterfaceUnit(reg).init();
        new AclUnit(reg).init();
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

    @Disabled
    @Test
    void testConnectivity() throws Exception {
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
                    .setErrorPatterns(Collections.emptySet());

            Cli io = ioConfigurationBuilder.getIO()
                    .toCompletableFuture()
                    .get();

            io.close();
        }
    }

    protected CliNode getCliNode() {
        return CLI_CFG;
    }

    @Disabled
    @Test
    void getInterfacesBA() throws Exception {
        ReadOnlyTransaction readOnlyTransaction = bindingBroker.newReadOnlyTransaction();
        final var read = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, IIDs.INTERFACES);
        Interfaces interfaces = read.get().get();

    }

    /**
     * Get all data from the device. Can serve for basic testing but also for performance tuning.
     */
    @Disabled
    @Test
    void getAllDOM() throws Exception {
        DOMDataReadOnlyTransaction readOnlyTransaction = domBroker.newReadOnlyTransaction();
        final var read = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.empty());
        NormalizedNode<?, ?> root = read.get().get();
    }

    @Disabled
    @SuppressWarnings("IllegalCatch")
    @Test
    void getAllDOMBenchmark() throws Exception {
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
}