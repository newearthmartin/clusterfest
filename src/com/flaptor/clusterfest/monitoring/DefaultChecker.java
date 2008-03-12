package com.flaptor.clusterfest.monitoring;

import java.util.Arrays;

/**
 * Default checker, that doesnt check anything yet
 * @author Martin Massera
 *
 */
public class DefaultChecker implements NodeChecker{

    public Result checkNode(MonitorNodeDescriptor node, NodeState state) {
        
        return new Result(Sanity.UNKNOWN, Arrays.asList(new String[]{"Node not checked"}));
    }

}
