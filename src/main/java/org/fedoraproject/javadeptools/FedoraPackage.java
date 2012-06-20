/*-
 * Copyright (c) 2012 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.javadeptools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;

class FedoraPackage {
  private final String name;
  private final Set<JavaClass> classes = new TreeSet<JavaClass>();

  private List<JavaClass> read_rpm(File rpm) throws IOException,
          InterruptedException {

    Process p = Runtime.getRuntime().exec(
            new String[] { "rpm2cpio", rpm + "" });
    InputStream is = p.getInputStream();

    CpioArchiveInputStream cpio_is = new CpioArchiveInputStream(is);
    ArchiveEntry cpio_ent;

    List<JavaClass> list = new ArrayList<JavaClass>();
    while ((cpio_ent = cpio_is.getNextEntry()) != null) {
      if (cpio_ent.isDirectory() || !cpio_ent.getName().endsWith(".jar"))
        continue;
      list.addAll(read_jar(cpio_is));
    }
    cpio_is.close();
    // If rpm2cpio hasn't terminated yet it should get SIGPIPE now.
    p.waitFor();
    return list;
  }

  private List<JavaClass> read_jar(InputStream is) throws IOException {
    List<JavaClass> list = new ArrayList<JavaClass>();
    JarInputStream jar_is = new JarInputStream(is);
    JarEntry jar_ent;
    while ((jar_ent = jar_is.getNextJarEntry()) != null) {
      if (jar_ent.isDirectory() || !jar_ent.getName().endsWith(".class"))
        continue;
      list.add(new JavaClass(jar_ent.getName(), jar_is));
    }
    return list;
  }

  public FedoraPackage(File f) throws IOException, InterruptedException {
    final String fn = f.getName();

    if (fn.endsWith(".rpm")) {
      classes.addAll(read_rpm(f));
      name = fn.replaceFirst("\\.rpm$", "").replaceAll("-[^-]*-[^-]*$", "");
    }

    else if (fn.endsWith(".jar")) {
      classes.addAll(read_jar(new FileInputStream(f)));
      name = "@jar@" + fn;
    }

    else {
      throw new IOException("unknown file suffix:" + f);
    }
  }

  public boolean isJavaPackage() {
    return !classes.isEmpty();
  }

  public String getName() {
    return name;
  }

  Collection<JavaClass> getClasses() {
    return Collections.unmodifiableCollection(classes);
  }
}
