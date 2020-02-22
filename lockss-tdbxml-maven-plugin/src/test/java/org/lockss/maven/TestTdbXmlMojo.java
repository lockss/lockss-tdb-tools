/*
 * Copyright (c) 2020 Board of Trustees of Leland Stanford Jr. University,
 * all rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Stanford University shall not
 * be used in advertising or otherwise to promote the sale, use or other dealings
 * in this Software without prior written authorization from Stanford University.
 */

package org.lockss.maven;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import org.lockss.maven.TdbXmlMojo.TdbFindFile;

@RunWith(MockitoJUnitRunner.class)
public class TestTdbXmlMojo extends AbstractMojoTestCase {

  File srcDir = getTestFile("target/test-classes/org/lockss/maven/tdb");
  File dstDir = getTestFile("target/test-classes/org/lockss/maven/xml");
  @Mock
  List<String> compilePath;
  @Mock
  Log log;
  @Mock
  Map pluginContext;

  @InjectMocks
  TdbXmlMojo tdbXmlMojo;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    if(dstDir.exists()) {
      FileUtils.cleanDirectory(dstDir);
      Files.delete(dstDir.toPath());
    }
    super.tearDown();
  }


  @Test(expected = MojoExecutionException.class)
  public void testCheckDirsInvalid() throws Exception {
    tdbXmlMojo.checkDirs();
  }

  @Test
  public void testCheckDirsValid() throws Exception {
    tdbXmlMojo.setSrcDir(srcDir);
    tdbXmlMojo.setDstDir(dstDir);
    tdbXmlMojo.checkDirs();
  }


  @Test
  public void testExecJavaCmd() throws Exception {
    Map<File, File> inMap = new HashMap<>();
    File in1 = new File(srcDir, "sample1.tdb");
    File in2 = new File(srcDir, "tdb1.tdb");
    File out1 = new File(dstDir, "sample1.xml");
    File out2 = new File(dstDir, "tdb1.xml");
    inMap.put(in1, out1);
    inMap.put(in2, out2);
    tdbXmlMojo.setRecurse(true);
    tdbXmlMojo.setClasspath(System.getProperty("java.class.path"));
    tdbXmlMojo.execJavaCmd(inMap);

  }

  @Test
  public void testFindFile() throws Exception {
    Map<File, File> convMap = new HashMap<>();
    TdbFindFile finder = new TdbFindFile(srcDir, dstDir, convMap, null);
    int depth = Integer.MAX_VALUE;
    EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
    // no links
    finder = new TdbFindFile(srcDir, dstDir, convMap, null);
    Files.walkFileTree(srcDir.toPath(), opts, depth, finder);
    assertEquals(2, convMap.size());
  }

  @Test
  public void testFindFileSymLink() throws Exception {
    Map<File, File> convMap = new HashMap<>();
    File linkDir = getTestFile("target/test-classes/org/lockss/maven/tdb_link");

    TdbFindFile finder = new TdbFindFile(linkDir, dstDir, convMap, null);
    int depth = Integer.MAX_VALUE;
    EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
    finder = new TdbFindFile(linkDir, dstDir, convMap, null);
    Files.walkFileTree(srcDir.toPath(), opts, depth, finder);
    assertEquals(2, convMap.size());
  }

  @Test
  public void testMojoGoal() throws Exception {
    File testPom = getTestFile("src/test/resources/simple-pom.xml");
    assertNotNull(testPom);
    assertTrue(testPom.exists());
    TdbXmlMojo my_mojo = (TdbXmlMojo) lookupMojo("tdbxml", testPom);
    assertNotNull(my_mojo);
//    my_mojo.execute();
  }
}