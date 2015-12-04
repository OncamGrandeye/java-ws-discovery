/*
MatchScopeRFC2396.java

Copyright (C) 2008-2009 Magnus Skjegstad

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ms.wsdiscovery.servicedirectory.matcher;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;

import com.ms.wsdiscovery.servicedirectory.WsDiscoveryService;
import com.ms.wsdiscovery.xml.jaxb_generated.ScopesType;

/**
 * Match scope against target service using the RFC3986 algorithm.
 * See the WS-Discovery specification or {@link MatchBy} for details.
 * @author Steven Dillingham
 */
public class MatchScopeRFC3986 implements IMatchScope {

    /**
     * Match scope against target service using the RFC3986 algorithm.
     * See the WS-Discovery specification or {@link MatchBy} for details.
     * @param target Target service.
     * @param probeScopes Scopes to probe for.
     * @return True on success, false on failure.
     */
    public boolean matchScope(WsDiscoveryService target, ScopesType probeScopes) {
        // All scopes in probe must match target
        if (probeScopes != null)
            for (String probescope : probeScopes.getValue()) {
                boolean match = false;
                URI probeuri = URI.create(probescope);

                // Loop through scopes in target to find a match
                for (URI targetscope : target.getScopes())
                    if (matchURIByRFC3986(targetscope, probeuri)) {
                        match = true;
                        break;
                    }

                // No match found for this probe scope, return false
                if (!match)
                    return false;
            }

        // All scopes in probe matched
        return true;
    }

    /**
     * Helper for {@link MatchScopeRFC3986#matchURIByRFC3986(java.net.URI, java.net.URI)}. Matches
     * a scope in the target service against a scope in the probe.
     * @param target Target service scope URI.
     * @param probe Probe scope URI.
     * @return True on success, false on failure.
     */
    protected boolean matchURIByRFC3986(URI target, URI probe) {
        // See WsDiscovery, section 5.1 for details
        if (((target != null) && (probe != null)) && // Fail if one or both of the parameters are null
                ((target.getScheme() != null) && (probe.getScheme() != null)) && // Fail if the scheme is not set
                (target.getScheme().toUpperCase().equals(probe.getScheme().toUpperCase()))) // Compare scheme
            if (target.getAuthority().toUpperCase().equals(probe.getAuthority().toUpperCase())) { // Compare server (authority)
                String[] targetsegments = normalizeUriPath(target.getPath());// target.getPath().split("/"); // Split path into segments
                String[] probesegments = normalizeUriPath(probe.getPath());// probe.getPath().split("/");

                if (probesegments.length <= targetsegments.length) { // If probe has more segments than target, a match is not possible
                    for (int i = 0; i < probesegments.length; i++) // Check if each segment in probe matches a segment in target
                        if (!probesegments[i].equals(targetsegments[i]))
                            return false;
                    // I.e http://example.com/abc matches http://example.com/abc/def,
                    // but http://example.com/a does not.
                } else
                    return false;

                // If we get here, all tests are ok - return true
                return true;
            }

        return false;
    }

    /**
     * Helper for {@link MatchScopeRFC3986#normalizeUriPath(String)}. Normalized the
     * path according to RFC3986 (Percent-Encoding & Path Segment).
     * @param path The Path of a {@link URI}.
     * @return String array of the normalized path.
     */
    protected String [] normalizeUriPath(String path) {

        String[] retVal;

        try {
            // Percent-Encoding normalization
            retVal = URLDecoder.decode(path, "UTF-8").split("/");
        } catch (UnsupportedEncodingException e) {
            retVal = path.toLowerCase().split("/");
        }

        ArrayList<String> normalizedSegments = new ArrayList<String>();

        // Loop through and deal with Path normalization
        for (String segment : retVal) {

            // Ignore the 'current path' segment
            if (segment.equals(".") == false) {

                // Handle the 'parent path' segment
                if (segment.equals("..")) {
                    // Remove the last segment if one is present
                    if (normalizedSegments.size() > 0) {
                        normalizedSegments.remove(normalizedSegments.size() - 1);
                    }
                } else {
                    normalizedSegments.add(segment);
                }
            }

        }

        retVal = new String[normalizedSegments.size()];
        retVal = normalizedSegments.toArray(retVal);
        return retVal;
    }
}
