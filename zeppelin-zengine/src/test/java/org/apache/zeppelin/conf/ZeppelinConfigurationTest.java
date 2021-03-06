/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.conf;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.zeppelin.conf.ZeppelinConfiguration.ConfVars;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ZeppelinConfigurationTest {
    @Before
    public void clearSystemVariables() {
        System.clearProperty(ConfVars.ZEPPELIN_NOTEBOOK_DIR.getVarName());
    }

    @Test
    public void getAllowedOrigins2Test() throws MalformedURLException, ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/test-zeppelin-site2.xml"));
        List<String> origins = conf.getAllowedOrigins();
        Assert.assertEquals(2, origins.size());
        Assert.assertEquals("http://onehost:8080", origins.get(0));
        Assert.assertEquals("http://otherhost.com", origins.get(1));
    }

    @Test
    public void getAllowedOrigins1Test() throws MalformedURLException, ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/test-zeppelin-site1.xml"));
        List<String> origins = conf.getAllowedOrigins();
        Assert.assertEquals(1, origins.size());
        Assert.assertEquals("http://onehost:8080", origins.get(0));
    }

    @Test
    public void getAllowedOriginsNoneTest() throws MalformedURLException, ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        List<String> origins = conf.getAllowedOrigins();
        Assert.assertEquals(1, origins.size());
    }

    @Test
    public void isWindowsPathTestTrue() throws ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        Boolean isIt = conf.isWindowsPath("c:\\test\\file.txt");
        Assert.assertTrue(isIt);
    }

    @Test
    public void isWindowsPathTestFalse() throws ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        Boolean isIt = conf.isWindowsPath("~/test/file.xml");
        Assert.assertFalse(isIt);
    }

    @Test
    public void isPathWithSchemeTestTrue() throws ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        Boolean isIt = conf.isPathWithScheme("hdfs://hadoop.example.com/zeppelin/notebook");
        Assert.assertTrue(isIt);
    }

    @Test
    public void isPathWithSchemeTestFalse() throws ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        Boolean isIt = conf.isPathWithScheme("~/test/file.xml");
        Assert.assertFalse(isIt);
    }

    @Test
    public void isPathWithInvalidSchemeTest() throws ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        Boolean isIt = conf.isPathWithScheme("c:\\test\\file.txt");
        Assert.assertFalse(isIt);
    }

    @Test
    public void getNotebookDirTest() throws ConfigurationException {
        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        String notebookLocation = conf.getNotebookDir();
        assertTrue(notebookLocation.endsWith("notebook"));
    }

    @Test
    public void isNotebookPublicTest() throws ConfigurationException {

        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        boolean isIt = conf.isNotebookPublic();
        assertTrue(isIt);
    }

    @Test
    public void getPathTest() throws ConfigurationException {
        System.setProperty(ConfVars.ZEPPELIN_HOME.getVarName(), "/usr/lib/zeppelin");
        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        Assert.assertEquals("/usr/lib/zeppelin", conf.getZeppelinHome());
        Assert.assertEquals("/usr/lib/zeppelin/conf", conf.getConfDir());
    }

    @Test
    public void getConfigFSPath() throws ConfigurationException {
        System.setProperty(ConfVars.ZEPPELIN_HOME.getVarName(), "/usr/lib/zeppelin");
        System.setProperty(ConfVars.ZEPPELIN_CONFIG_FS_DIR.getVarName(), "conf");
        ZeppelinConfiguration conf = new ZeppelinConfiguration(this.getClass().getResource("/zeppelin-test-site.xml"));
        assertEquals("/usr/lib/zeppelin/conf", conf.getConfigFSDir(true));

        System.setProperty(ConfVars.ZEPPELIN_CONFIG_STORAGE_CLASS.getVarName(), "org.apache.zeppelin.storage.FileSystemConfigStorage");
        assertEquals("conf", conf.getConfigFSDir(false));
    }
}
