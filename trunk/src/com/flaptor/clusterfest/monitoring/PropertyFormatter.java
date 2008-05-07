package com.flaptor.clusterfest.monitoring;

import com.flaptor.clusterfest.NodeDescriptor;

public interface PropertyFormatter {
    
    String format(NodeDescriptor node, String propertyName, Object propertyValue);
}
