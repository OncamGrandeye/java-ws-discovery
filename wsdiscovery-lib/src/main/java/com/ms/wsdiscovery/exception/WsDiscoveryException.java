/*
WsDiscoveryException.java

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

package com.ms.wsdiscovery.exception;

/**
 * Base class for exceptions returned by WS-Discovery.
 * 
 * @author Magnus Skjegstad
 */
public class WsDiscoveryException extends Exception {

    /**
     * Creates a new instance of <code>WsDiscoveryException</code> without detail message.
     */
    public WsDiscoveryException() {
    }

    /**
     * Constructs an instance of <code>WsDiscoveryException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public WsDiscoveryException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>WsDiscoveryException</code> with the specified detail message and cause.
     * @param msg the detail message.
     * @param cause the throwable that caused the exception.
     */
    public WsDiscoveryException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
