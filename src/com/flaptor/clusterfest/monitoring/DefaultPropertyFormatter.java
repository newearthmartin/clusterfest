package com.flaptor.clusterfest.monitoring;

import com.flaptor.util.StringUtil;

/**
 * Default property formatter that changes newlines to <br/>
 * @author Martin Massera
 *
 */
public class DefaultPropertyFormatter implements PropertyFormatter {

    public String format(String name, Object value) {
        return StringUtil.whitespaceToHtml(value.toString());
    }

}
