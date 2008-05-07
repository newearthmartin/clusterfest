package com.flaptor.clusterfest.monitoring;

import com.flaptor.util.StringUtil;

/**
 * Default property formatter that changes newlines to <br/>
 * @author Martin Massera et al ;)
 *
 */
public class DefaultPropertyFormatter implements PropertyFormatter {

    public String format(String name, Object value) {
    	if (name.equals("freeMemory") || name.equals("totalMemory") || name.equals("usedMemory")) {
    		return bytesToString(((Long)value).longValue());
    	}
        return StringUtil.whitespaceToHtml(value.toString());
    }
    
    /**
     * This method returns a nice representation of longs in the format
     * G,M,k,
     */
    protected String bytesToString(long value) {
    	long bytes = value % 1024;
    	value /= 1024;
    	long kbytes = value % 1024;
    	value /= 1024;
    	long mbytes = value % 1024;
    	value /= 1024;
    	long gbytes = value;
    	StringBuffer buf = new StringBuffer();
    	if (0 != gbytes) {
    		buf.append(gbytes).append("G ");
    	}
    	if (0 != mbytes) {
    		buf.append(mbytes).append("M ");
    	}
    	if (0 != kbytes) {
    		buf.append(kbytes).append("k ");
    	}
    	buf.append(bytes);
    	return buf.toString();
    }

}
