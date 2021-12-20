package com.zk.op;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ClientCacheManager {

    private static final Map<HttpSession, ZookeeperWatcher> CLIENT_MAP = new HashMap<>(100);

    private static final String PRE = "zk-client-";

    public static ZooKeeper getClient() {
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
        ZookeeperWatcher watcher = CLIENT_MAP.get(session);
        if (Objects.nonNull(watcher) && watcher.getState().isAlive()) {
            return watcher.getClient();
        }
        String cxnStr = (String) session.getAttribute("cxnStr");
        String key = PRE + cxnStr;
        Object obj = session.getAttribute(key);
        if (Objects.isNull(obj)) {
            watcher = new ZookeeperWatcher(cxnStr);
            CLIENT_MAP.put(session, watcher);
        } else {
            watcher = (ZookeeperWatcher) obj;
        }
        return watcher.getClient();
    }

}
