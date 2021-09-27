package appServer.handlersForMonitoringSrv;

import appServer.EntityUser;
import lombok.Getter;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceForMonitoring {
    @Getter
    private final AtomicInteger countConnection=new AtomicInteger(0);
    @Getter
    private final CopyOnWriteArrayList<EntityUser> users = new CopyOnWriteArrayList<>();

}
