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
import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

@Mojo(name = "tdbxml", requiresDependencyResolution = ResolutionScope.TEST)
public class TdbXmlMojo extends AbstractMojo {

  @Parameter(property = "skip", defaultValue = "false")
  private boolean skip;
  
  @Parameter(property = "srcDir")
  private File srcDir;

  @Parameter(property = "dstDir")
  private File dstDir;

  @Parameter(property = "recurse", defaultValue = "true")
  private boolean recurse;

  @Parameter(property= "throwOnFail", defaultValue = "true")
  private boolean throwOnFail;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  private String classpath;

  public void setSkip(boolean skip) {
    this.skip = skip;
  }
  
  public void setSrcDir(File srcDir) {
    this.srcDir = srcDir;
  }

  public void setDstDir(File dstDir) {
    this.dstDir = dstDir;
  }

  public void setRecurse(boolean recurse) {
    this.recurse = recurse;
  }
  public void setThrowOnFail(boolean throwOnFail) {
    this.throwOnFail = throwOnFail;
  }

  public void setClasspath(String classpath){this.classpath = classpath; }

  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Skipping lockss-tdbxml-maven-plugin per configuration");
      return;
    }
    
    try {
      classpath = String.join(":", project.getTestClasspathElements());
      getLog().debug("Classpath: " + classpath);
    } catch (DependencyResolutionRequiredException exc) {
      throw new MojoExecutionException("Error while determining the classpath elements", exc);
    }
    checkDirs();
    Path startPath = srcDir.toPath();
    Map<File, File> convMap = new HashMap<>();
    EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
    int depth = 1;
    if (recurse) {
      depth = Integer.MAX_VALUE;
    }
    TdbFindFile finder = new TdbFindFile(srcDir, dstDir, convMap, getLog());
    try {
      getLog().info("Starting tree walk...");
      Files.walkFileTree(startPath, opts, depth, finder);
    } catch (IOException exc) {
      throw new MojoExecutionException("Unable to walk source tree: " + exc.getMessage(), exc);
    }
    getLog().info(String.format("Found %d TDB files", convMap.size()));
    if (!convMap.isEmpty()) {
      execJavaCmd(convMap);
    }
  }

  public void checkDirs() throws MojoExecutionException {
    if (srcDir == null || !srcDir.exists() || !srcDir.canRead()) {
      throw new MojoExecutionException("Invalid srcDir value: " + srcDir);
    }

    if (dstDir == null || (!dstDir.exists() && !dstDir.mkdirs())) {
      throw new MojoExecutionException("Invalid dstDir value: " + dstDir);
    }
  }

  public void checkOutDir(File dir) throws MojoExecutionException {
    if (dir == null || (!dir.exists() && !dir.mkdirs())) {
      throw new MojoExecutionException("Can't create output dir: " + dir);
    }
  }

  public void execJavaCmd(Map<File, File> mapFiles) throws MojoExecutionException {
//    args.add("java"); // command name
//    args.add("-Xmx1024m");
//    args.add("-classpath");
//    args.add(classpath);
//    args.add("org.lockss.tdb.TdbXml");
    if (recurse) {
      int convCount = 0;
      for (Map.Entry<File, File> tofro : mapFiles.entrySet()) {
	List<String> args = new ArrayList<>();
        File srcFile = tofro.getKey();
        File dstFile = tofro.getValue();
	checkOutDir(dstFile.getParentFile());
        if (isOutOfDate(srcFile, dstFile)) {
          getLog().debug(String.format("Converting %s => %s", srcFile.getAbsolutePath(), dstFile.getAbsoluteFile()));
          args.add("-A");
          args.add("-i");
          args.add(srcFile.getAbsolutePath());
          args.add("-o");
          args.add(dstFile.getAbsolutePath());
          if(runProcess(args) != 0 && throwOnFail) {
            throw new MojoExecutionException("Conversion failed");
          }
          convCount++;
	} else {
          getLog().debug(String.format("Skipping %s => %s", srcFile.getAbsolutePath(), dstFile.getAbsoluteFile()));
        }
      }
      getLog().info(String.format("Converted %d TDB files", convCount));
    } else {
      List<String> args = new ArrayList<>();
      args.add("--all");
      args.add("--output-dir=" + dstDir);
      for (File file : mapFiles.keySet()) {
        args.add(file.getAbsolutePath());
      }
      if(runProcess(args) != 0 && throwOnFail) {
        throw new MojoExecutionException("Conversion failed");
      }
      getLog().info("Converted all TDB files");
    }
  }

  private int runProcess(List<String> args) throws MojoExecutionException {
    try {
      new org.lockss.tdb.TdbXml().run(args.toArray(new String[0]));
      return 0;
    }
    catch (Exception exc) {
      getLog().error("TdbXml invocation threw", exc);
      return 1;
    }
//    int exitVal;
//    ProcessBuilder pb = new ProcessBuilder(args);
//    pb.redirectErrorStream(true);
//    Process p;
//    try {
//      p = pb.start();
//      BufferedReader br=new BufferedReader(
//          new InputStreamReader(
//              p.getInputStream()));
//      String line;
//      while((line=br.readLine())!=null){
//        getLog().info(line);
//      }
//      p.waitFor();
//      exitVal = p.exitValue();
//
//    } catch (InterruptedException e) {
//      throw new MojoExecutionException("interrupted!", e);
//    } catch (IOException e) {
//      throw new MojoExecutionException("failed to start process", e);
//    }
//    getLog().debug("process exited with " + exitVal);
//    return exitVal;
  }

  private boolean isOutOfDate(File srcFile, File dstFile) {
    if (!dstFile.exists()) {
      return true;
    }
    Date srcModified = new Date(srcFile.lastModified());
    Date destModified = new Date(dstFile.lastModified());
    return srcModified.after(destModified);
  }

  static final class TdbFindFile extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private final Log logger;
    private final String src;
    private final String dst;
    Map<File, File> resMap;

    TdbFindFile(File src, File dst, Map<File, File> resMap, Log log) {
      matcher = FileSystems.getDefault().getPathMatcher("glob:" + "*.tdb");
      logger = log;
      this.src = src.getPath();
      this.dst = dst.getPath();
      this.resMap = resMap;
    }

    @Override
    public FileVisitResult visitFile(
        Path file, BasicFileAttributes attrs
    ) throws IOException {
      process(file);
      return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
        IOException exc) {
      if (logger != null)logger.error(exc);
      return CONTINUE;
    }


    /**
     * Compare the glob pattern against the file name
     */
    void process(Path file) {
      Path name = file.getFileName();
      if (name != null && matcher.matches(name)) {
        File tdb= file.toFile();
        File xml = new File(tdb.getPath().replaceFirst(src, dst).
            replace(".tdb", ".xml"));
        resMap.put(tdb, xml);
      }
    }
  }
}

