package com.zk.test.o;

import com.alibaba.fastjson.JSON;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZkClientTest {
    static {
        System.setProperty("zookeeper.preAllocSize", "1024");// 1M data log
    }

    final Logger logger = LoggerFactory.getLogger(ZkClientTest.class);
    //
    private ZkClient client;
    final int TIMEOUT = 30;//30 second for loop timeout

    //
    private static void deleteFile(File f) throws IOException {
        if (f.isFile()) {
            f.delete();
            //System.out.println("[DELETE FILE] "+f.getPath());
        } else if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File fs : files) {
                    deleteFile(fs);
                }
            }
            f.delete();
            //System.out.println("[DELETE DIRECTORY] "+f.getPath());
        }
    }

    @AfterClass
    public static void cleanup() throws IOException {
        deleteFile(new File(".", "build/test").getCanonicalFile());
    }

    @Before
    public void setUp() {
        this.client = new ZkClient("10.0.4.160:2181,10.0.4.160:3181,10.0.4.160:4181");
    }


    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#subscribeChildChanges(String, IZkChildListener)}
     * .
     */
    @Test
    public void testSubscribeChildChanges() throws Exception {
        final String path = "/a";
        final AtomicInteger count = new AtomicInteger(0);
        final ArrayList<String> children = new ArrayList<String>();
        IZkChildListener listener = (parentPath, currentChildren) -> {
            count.incrementAndGet();
            children.clear();
            if (currentChildren != null)
                children.addAll(currentChildren);
            logger.info("handle childchange " + parentPath + ", " + currentChildren);
        };
        //
        client.subscribeChildChanges(path, listener);
        //
        logger.info("create the watcher node " + path);
        client.createPersistent(path);
        //wait some time to make sure the event was triggered
        TestUtil.waitUntil(1, count::get, TimeUnit.SECONDS, TIMEOUT);
        //
        assertEquals(1, count.get());
        assertEquals(0, children.size());
        //
        //create a child node
        count.set(0);
        client.createPersistent(path + "/child1");
        logger.info("create the first child node " + path + "/child1");
        TestUtil.waitUntil(1, count::get, TimeUnit.SECONDS, TIMEOUT);
        //
        assertEquals(1, count.get());
        assertEquals(1, children.size());
        assertEquals("child1", children.get(0));
        //
        // create another child node and delete the node
        count.set(0);
        logger.info("create the second child node " + path + "/child2");
        client.createPersistent(path + "/child2");
        //
        logger.info("delete the watcher node " + path);
        client.deleteRecursive(path);
        //
        Boolean eventReceived = TestUtil.waitUntil(true, () -> count.get() > 0 && children.size() == 0, TimeUnit.SECONDS, TIMEOUT);
        assertTrue(eventReceived);
        assertEquals(0, children.size());
        // ===========================================
        // do it again and check the listener validate
        // ===========================================
        count.set(0);
        //
        logger.info("create the watcher node again " + path);
        client.createPersistent(path);
        //
        eventReceived = TestUtil.waitUntil(true, () -> count.get() > 0, TimeUnit.SECONDS, TIMEOUT);
        assertTrue(eventReceived);
        assertEquals(0, children.size());
        //
        // now create the first node
        count.set(0);
        final String child3 = "/child3";
        client.createPersistent(path + child3);
        logger.info("create the first child node again " + path + child3);
        //
        eventReceived = TestUtil.waitUntil(true, () -> count.get() > 0, TimeUnit.SECONDS, 15);
        assertTrue(eventReceived);
        assertEquals(1, children.size());
        assertEquals("child3", children.get(0));
        //
        // delete root node
        count.set(0);
        logger.info("delete the watcher node again " + path);
        client.deleteRecursive(path);
        // This will receive two message: (1) child was deleted (2) parent was deleted
        //
        eventReceived = TestUtil.waitUntil(true, children::isEmpty, TimeUnit.SECONDS, TIMEOUT);
        assertTrue(eventReceived);
        assertTrue(children.isEmpty());
    }

    static class Holder<T> {
        T t;

        public void set(T t) {
            this.t = t;
        }

        public T get() {
            return t;
        }
    }

    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#subscribeDataChanges(java.lang.String, org.I0Itec.zkclient.IZkDataListener)}
     * .
     */
    @Test
    public void testSubscribeDataChanges() throws Exception {
        String path = "/a";
        final AtomicInteger countChanged = new AtomicInteger(0);
        final AtomicInteger countDeleted = new AtomicInteger(0);
        final Holder<String> holder = new Holder<>();
        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) {
                countDeleted.incrementAndGet();
                holder.set(JSON.toJSONString(data));
            }

            public void handleDataDeleted(String dataPath) {
                countDeleted.incrementAndGet();
                holder.set(null);
            }

        };
        client.subscribeDataChanges(path, listener);
        //
        // create the node
        client.createPersistent(path, "aaa");
        //
        //wait some time to make sure the event was triggered
        TestUtil.waitUntil(1, countChanged::get, TimeUnit.SECONDS, TIMEOUT);
        assertEquals(1, countChanged.get());
        assertEquals(0, countDeleted.get());
        assertEquals("aaa", holder.get());
        //
        countChanged.set(0);
        countDeleted.set(0);
        //
        client.delete(path);
        TestUtil.waitUntil(1, countDeleted::get, TimeUnit.SECONDS, TIMEOUT);
        assertEquals(0, countChanged.get());
        assertEquals(1, countDeleted.get());
        assertNull(holder.get());
        // ===========================================
        // do it again and check the listener validate
        // ===========================================
        countChanged.set(0);
        countDeleted.set(0);
        client.createPersistent(path, "bbb");
        TestUtil.waitUntil(1, countChanged::get, TimeUnit.SECONDS, TIMEOUT);
        assertEquals(1, countChanged.get());
        assertEquals("bbb", holder.get());
        //
        countChanged.set(0);
        client.writeData(path, "ccc");
        //
        TestUtil.waitUntil(1, countChanged::get, TimeUnit.SECONDS, TIMEOUT);
        assertEquals(1, countChanged.get());
        assertEquals("ccc", holder.get());
    }


    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#createPersistent(java.lang.String, boolean)}
     * .
     */
    @Test
    public void testCreatePersistent() {
        final String path = "/a/b";
        try {
            client.createPersistent(path, false);
            fail("should throw exception");
        } catch (ZkNoNodeException e) {
            assertFalse(client.exists(path));
        }
        client.createPersistent(path, true);
        assertTrue(client.exists(path));
    }

    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#createPersistent(String, Object)}
     * .
     */
    @Test
    public void testCreatePersistentStringByteArray() {
        String path = "/a";
        client.createPersistent(path, "abc");
        assertEquals("abc", JSON.toJSONString(client.readData(path)));
        //
    }

    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#createPersistentSequential(String, Object)}
     * .
     */
    @Test
    public void testCreatePersistentSequential() {
        String path = "/a";
        String npath = client.createPersistentSequential(path, "abc");
        assertTrue(npath != null && npath.length() > 0);
        npath = client.createPersistentSequential(path, "abc");
        assertEquals("abc", JSON.toJSONString(client.readData(npath)));
    }

    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#createEphemeral(java.lang.String)}.
     */
    @Test
    public void testCreateEphemeralString() {
        String path = "/a";
        client.createEphemeral(path);
        Stat stat = new Stat();
        client.readData(path, stat);
        assertTrue(stat.getEphemeralOwner() > 0);
    }

    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#create(String, Object, List, CreateMode)}
     * <p>
     * .
     */
    @Test
    public void testCreate() {
        String path = "/a";
        String abc = client.create(path, "abc", CreateMode.PERSISTENT);
        assertEquals("abc", client.readData(path));
    }


    @Test
    public void testCreateEphemeralSequential() {
        String path = "/a";
        String npath = client.createEphemeralSequential(path, "abc");
        assertTrue(npath != null && npath.startsWith("/a"));
        Stat stat = new Stat();
        assertTrue(stat.getEphemeralOwner() > 0);
    }


    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#getChildren(java.lang.String)}.
     */
    @Test
    public void testGetChildrenString() {
        String path = "/a";
        client.createPersistent(path + "/ch1", true);
        client.createPersistent(path + "/ch2");
        client.createPersistent(path + "/ch3");
        List<String> children = client.getChildren(path);
        assertEquals(3, children.size());
        assertEquals(3, client.countChildren(path));
        assertNull(client.getChildren("/aaa"));
    }


    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#exists(java.lang.String)}.
     */
    @Test
    public void testExistsString() {
        String path = "/a";
        assertFalse(client.exists(path));
        client.createPersistent(path);
        assertTrue(client.exists(path));
        client.delete(path);
        assertFalse(client.exists(path));
    }

    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#deleteRecursive(java.lang.String)}.
     */
    @Test
    public void testDeleteRecursive() {
        String path = "/a/b/c";
        client.createPersistent(path, true);
        assertTrue(client.exists(path));
        assertTrue(client.deleteRecursive("/a"));
        assertFalse(client.exists(path));
        assertFalse(client.exists("/a/b"));
        assertFalse(client.exists("/a"));
    }

    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#waitUntilExists(java.lang.String, java.util.concurrent.TimeUnit, long)}
     * .
     */
    @Test
    public void testWaitUntilExists() {
        final String path = "/a";
        new Thread(() -> {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
            client.createPersistent(path);
        }).start();
        assertTrue(client.waitUntilExists(path, TimeUnit.SECONDS, 10));
        assertTrue(client.exists(path));
        //
        assertFalse(client.waitUntilExists("/notexists", TimeUnit.SECONDS, 1));
    }

    /**
     * Test method for {@link org.I0Itec.zkclient.ZkClient#waitUntilConnected()}
     * .
     */
    @Test
    public void testWaitUntilConnected() {
        ZkClient client2 = new ZkClient("localhost:4711", 15000);
        client2.waitUntilConnected();
        //
        assertTrue(client2.waitForKeeperState(KeeperState.Disconnected, 1, TimeUnit.SECONDS));
        //
        assertFalse(client2.waitUntilConnected(1, TimeUnit.SECONDS));
        client2.close();
    }


    /**
     * Test method for
     * {@link org.I0Itec.zkclient.ZkClient#readData(java.lang.String, org.apache.zookeeper.data.Stat)}
     * .
     */
    @Test
    public void testReadDataStringStat() {
        client.createPersistent("/a", "data".getBytes());
        Stat stat = new Stat();
        byte[] a = client.readData("/a", stat);
        System.out.println(a);
        assertEquals(0, stat.getVersion());
        assertTrue(stat.getDataLength() > 0);
    }


    /**
     * Test method for {@link org.I0Itec.zkclient.ZkClient#numberOfListeners()}.
     */
    @Test
    public void testNumberOfListeners() {
        IZkChildListener zkChildListener = (parentPath, currentChilds) -> {
        };
        client.subscribeChildChanges("/", zkChildListener);
        assertEquals(1, client.numberOfListeners());
        //
        IZkDataListener zkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {

            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {

            }
        };
        client.subscribeDataChanges("/a", zkDataListener);
        assertEquals(2, client.numberOfListeners());
        //
        client.subscribeDataChanges("/b", zkDataListener);
        assertEquals(3, client.numberOfListeners());
        //
        IZkStateListener zkStateListener = new IZkStateListener() {
            @Override
            public void handleStateChanged(KeeperState state) throws Exception {

            }

            @Override
            public void handleNewSession() throws Exception {

            }

            @Override
            public void handleSessionEstablishmentError(Throwable error) throws Exception {

            }
        };
        client.subscribeStateChanges(zkStateListener);
        assertEquals(4, client.numberOfListeners());
        //
        client.unsubscribeChildChanges("/", zkChildListener);
        assertEquals(3, client.numberOfListeners());
        //
        client.unsubscribeAll();
        assertEquals(0, client.numberOfListeners());
    }


    @Test
    public void testChildListenerAfterSessionExpiredException() throws Exception {
        final int sessionTimeout = 200;
        this.client.createPersistent("/root");
        //
        int port = PortUtils.checkAvailablePort(4712);
        Gateway gateway = new Gateway(port, 123);
        gateway.start();
        //
        final ZkClient disconnectedClient = new ZkClient("localhost:" + port, sessionTimeout, 15000);
        final Holder<List<String>> children = new Holder<List<String>>();
        disconnectedClient.subscribeChildChanges("/root", new IZkChildListener() {

            @Override
            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
                children.set(currentChildren);
            }
        });
        gateway.stop();//
        //
        // the connected client created a new child node
        this.client.createPersistent("/root/node1");
        //
        // wait for 3x sessionTImeout, the session should have expired
        Thread.sleep(3 * sessionTimeout);
        //
        // now start the gateway
        gateway.start();
        //
        Boolean hasOneChild = TestUtil.waitUntil(true, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return children.get() != null && children.get().size() == 1;
            }
        }, TimeUnit.SECONDS, TIMEOUT);
        //
        assertTrue(hasOneChild);
        assertEquals("node1", children.get().get(0));
        assertEquals("node1", disconnectedClient.getChildren("/root").get(0));
        //
        disconnectedClient.close();
        gateway.stop();
    }

}
