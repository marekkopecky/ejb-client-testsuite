package org.wildfly.ejbclient.testsuite.integration.multinode.servertocluster.security;

import jakarta.ejb.Remote;

@Remote
public interface IntermediaryBeanRemote {

	String callRemoteSecuredBean();

}
