package org.wildfly.ejbclient.testsuite.integration.multinode.loadbalancing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.wildfly.ejbclient.testsuite.integration.multinode.common.beans.whereami.WhereAmI;
import org.wildfly.ejbclient.testsuite.integration.multinode.common.beans.whereami.WhereAmIStateless;
import org.wildfly.ejbclient.testsuite.integration.multinode.environment.ContainerHelpers;
import org.wildfly.ejbclient.testsuite.integration.multinode.environment.DeploymentHelpers;
import org.wildfly.ejbclient.testsuite.integration.multinode.environment.ManagementHelpers;
import org.wildfly.ejbclient.testsuite.integration.multinode.environment.MiscHelpers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.naming.client.WildFlyInitialContextFactory;

import static org.wildfly.ejbclient.testsuite.integration.multinode.environment.Containers.CLUSTER1_NODE1;
import static org.wildfly.ejbclient.testsuite.integration.multinode.environment.Containers.CLUSTER1_NODE2;
import static org.wildfly.ejbclient.testsuite.integration.multinode.environment.ManagementHelpers.setLoggerPrefix;
import static org.wildfly.ejbclient.testsuite.integration.multinode.environment.MiscHelpers.isIPv6;

@SuppressWarnings({"ArquillianDeploymentAbsent"})
@RunWith(Arquillian.class)
@RunAsClient
public class LoadBalancingViaWildflyConfigXml_StatelessBean_TestCase {

    private static final JavaArchive deployment = createDeployment();
    private static OnlineManagementClient creaper_node1;
    private static OnlineManagementClient creaper_node2;


    @ArquillianResource
    private ContainerController containerController;

    private static Logger logger = Logger.getLogger(LoadBalancingViaWildflyConfigXml_StatelessBean_TestCase.class.getName());

    public static JavaArchive createDeployment() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "load-balancing-config.jar");
        jar.addClasses(WhereAmI.class, WhereAmIStateless.class);
        return jar;
    }

    @BeforeClass
    public static void prepare() throws IOException {
        creaper_node1 = ManagementHelpers
                .createCreaper(CLUSTER1_NODE1.bindAddress, CLUSTER1_NODE1.managementPort);
        creaper_node2 = ManagementHelpers
                .createCreaper(CLUSTER1_NODE2.bindAddress, CLUSTER1_NODE2.managementPort);
    }

    @AfterClass
    public static void afterClass() {
        ManagementHelpers.safeClose(creaper_node1, creaper_node2);
    }

    @Before
    public void start() throws Exception {
        setLoggerPrefix("CLUSTER1_NODE1", CLUSTER1_NODE1.homeDirectory, CLUSTER1_NODE1.configurationXmlFile);
        setLoggerPrefix("CLUSTER1_NODE2", CLUSTER1_NODE2.homeDirectory, CLUSTER1_NODE2.configurationXmlFile);
        containerController.start(CLUSTER1_NODE1.nodeName);
        containerController.start(CLUSTER1_NODE2.nodeName);

        DeploymentHelpers.deploy(deployment, creaper_node1);
        DeploymentHelpers.deploy(deployment, creaper_node2);
    }

    @Test
    public void loadBalancingWhenOneUrlIsProvided() throws Exception {
        setWildflyConfigXml("wildfly-config-cluster1-node1");
        final Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, WildFlyInitialContextFactory.class.getName());
        final InitialContext ejbCtx = new InitialContext(properties);
        try {
            final WhereAmI bean = (WhereAmI) ejbCtx
                    .lookup("ejb:/load-balancing-config/" + WhereAmIStateless.class.getSimpleName() + "!"
                            + WhereAmI.class.getName());

            List<String> invokedNodes = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                final String node = bean.getNode();
                logger.info("Invocation performed on node: " + node);
                invokedNodes.add(node);
            }
            final Map<String, List<String>> nodesToInvocations = invokedNodes
                    .stream()
                    .collect(Collectors.groupingBy(Function.identity()));
            Assert.assertTrue("Thanks to load balancing, " + CLUSTER1_NODE1 + " should be invoked at least once, but it wasn't",
                    nodesToInvocations.containsKey(CLUSTER1_NODE1.nodeName));
            Assert.assertTrue("Thanks to load balancing, " + CLUSTER1_NODE2 + " should be invoked at least once, but it wasn't",
                    nodesToInvocations.containsKey(CLUSTER1_NODE2.nodeName));
        } finally {
            MiscHelpers.safeCloseEjbClientContext(ejbCtx);
        }
    }


    @After
    public void cleanup() throws Exception {
        DeploymentHelpers.undeploy(deployment.getName(), creaper_node1);
        DeploymentHelpers.undeploy(deployment.getName(), creaper_node2);
        ContainerHelpers
                .stopContainers(containerController, CLUSTER1_NODE1.nodeName, CLUSTER1_NODE2.nodeName);
        setLoggerPrefix("", CLUSTER1_NODE1.homeDirectory, CLUSTER1_NODE1.configurationXmlFile);
        setLoggerPrefix("", CLUSTER1_NODE2.homeDirectory, CLUSTER1_NODE2.configurationXmlFile);
    }

    public void setWildflyConfigXml(String name) {
        if ( !isIPv6() ) {
            System.setProperty("wildfly.config.url", ClassLoader.getSystemResource(
                    "org/wildfly/ejbclient/testsuite/integration/multinode/transactions/" + name + ".xml").toString());
        } else {
            System.setProperty("wildfly.config.url", ClassLoader.getSystemResource(
                    "org/wildfly/ejbclient/testsuite/integration/multinode/transactions/" + name + "-ipv6.xml").toString());
        }
    }

}