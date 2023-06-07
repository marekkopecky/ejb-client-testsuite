/*
 * Copyright 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.ejbclient.testsuite.integration.basic.echo;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.ejbclient.testsuite.integration.basic.utils.jndi.BeanType;
import org.wildfly.ejbclient.testsuite.integration.basic.echo.echobean.EchoBeanRemote;
import org.wildfly.ejbclient.testsuite.integration.basic.echo.echobean.EchoBeanSingleton;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * @author Jan Martiska
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SingletonEchoTestCase extends BasicEchoAbstractTestCase {

    @Deployment
    public static WebArchive deployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "basic-echo-test.war");
        archive.addPackage(EchoBeanRemote.class.getPackage());
        return archive;
    }

    @Override
    protected Class<? extends EchoBeanRemote> getBeanClass() {
        return EchoBeanSingleton.class;
    }

    @Override
    protected BeanType getBeanType() {
        return BeanType.SINGLETON;
    }
}
