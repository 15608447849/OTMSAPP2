package otmsapp.ping.server.update;

import otmsapp.ping.tools.FrontNotification;
import otmsapp.ping.tools.HearServer;

public class UpdateService extends HearServer{

    @Override
    protected FrontNotification createForeNotification(FrontNotification.Build build) {
        return null;
    }

    @Override
    protected void executeTask() {

    }
}
