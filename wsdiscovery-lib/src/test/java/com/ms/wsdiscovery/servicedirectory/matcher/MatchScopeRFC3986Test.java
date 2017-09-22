package com.ms.wsdiscovery.servicedirectory.matcher;

import com.ms.wsdiscovery.WsDiscoveryBuilder;
import com.ms.wsdiscovery.servicedirectory.WsDiscoveryService;
import com.ms.wsdiscovery.xml.jaxb_generated.ScopesType;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @uthor Steven Dillingham
 */
public class MatchScopeRFC3986Test {

    private WsDiscoveryService service;
    private QName servicePortType;
    private String serviceScope;
    private String serviceXAddr;
    private MatchScopeRFC3986 instance;

    public MatchScopeRFC3986Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = new MatchScopeRFC3986();
        servicePortType = new QName("http://localhost/portType", "localPart", "ns");
        serviceScope = "onvif://www.onvif.org/Profile/Q/Operational";
        serviceXAddr = "http://10.0.0.1:1234/localPart";
        service = WsDiscoveryBuilder.createService(servicePortType, serviceScope, serviceXAddr);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of matchScope method, of class MatchScopeRFC3986.
     */
    @Test
    public void testMatchScope() {
        System.out.println("matchScope");
        ScopesType probeScopes = new ScopesType();

        probeScopes.getValue().add("ONVIF://www.onvif.org/Profile/Q/");
        probeScopes.getValue().add("onvif://www.ONVIF.org/Profile/Q/Operational");
        probeScopes.getValue().add("onvif://www.onvif.org/Pr%6Ffile/Q/%4fperational");
        probeScopes.getValue().add("onvif://www.onvif.org/Pr%6ffile/Q/%4Fperational");
        probeScopes.getValue().add("onvif://www.onvif.org/Profile/S/../Q/Operational");
        probeScopes.getValue().add("onvif://www.onvif.org/Profile/./Q/Operational");

        boolean expResult = true;
        boolean result = instance.matchScope(service, probeScopes);
        assertEquals(expResult, result);

        probeScopes.getValue().add("onvif://www.onvif.org/PROFILE/Q/");
        expResult = false;
        result = instance.matchScope(service, probeScopes);
        assertEquals(expResult, result);
    }

    /**
     * Test of matchURIByRFC3986 method, of class MatchScopeRFC3986.
     */
    @Test
    public void testMatchURIByRFC3986() throws URISyntaxException {
        System.out.println("matchURIByRFC3986");

        URI target = new URI("http://www.examples.com/a/b");
        URI probe = new URI("http://www.examples.com/a/b");
        MatchScopeRFC3986 instance = new MatchScopeRFC3986();
        boolean expResult = true;
        boolean result = instance.matchURIByRFC3986(target, probe);
        assertEquals(expResult, result);

        target = new URI("http://www.examples.com/a/b");
        probe = new URI("http://www.examples.com/a/b///");
        instance = new MatchScopeRFC3986();
        expResult = true;
        result = instance.matchURIByRFC3986(target, probe);
        assertEquals(expResult, result);

        target = new URI("http://www.examples.com/a/b");
        probe = new URI("http://www.examples.com/a/");
        instance = new MatchScopeRFC3986();
        expResult = true;
        result = instance.matchURIByRFC3986(target, probe);
        assertEquals(expResult, result);

        target = new URI("http://www.examples.com/a/b");
        probe = new URI("http://www.examples.com/aa");
        instance = new MatchScopeRFC3986();
        expResult = false;
        result = instance.matchURIByRFC3986(target, probe);
        assertEquals(expResult, result);

        target = new URI("http://www.a.com");
        probe = new URI("http://www.b.com");
        instance = new MatchScopeRFC3986();
        expResult = false;
        result = instance.matchURIByRFC3986(target, probe);
        assertEquals(expResult, result);

        target = new URI("http://www.examples.com/a/b");
        probe = new URI("http://www.examples.com/a/b/c/");
        instance = new MatchScopeRFC3986();
        expResult = false;
        result = instance.matchURIByRFC3986(target, probe);
        assertEquals(expResult, result);

    }

}
