package it.unipr.aotlab.blogracy.web;

import sun.misc.Regexp;

import java.util.HashMap;
import java.util.Map;

/**
 * Enrico Franchi, 2011 (mc)a
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 11/2/11
 * Time: 11:54 AM
 */
public class UrlMapper {
    Map<Regexp, String> mapper = new HashMap<Regexp, String>();


    public UrlMapper(Map<String, String> map) {
        for(Map.Entry<String, String> entry: map.entrySet()) {
            Regexp rex = new Regexp(entry.getKey());
            add(rex, entry.getValue());
        }
    }

    private void add(Regexp urlMap, String className) {
        mapper.put(urlMap, className);
    }


}
