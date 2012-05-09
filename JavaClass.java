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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;

/**
 * This class represents a Java class.
 * 
 * @author Mikolaj Izdebski <mizdebsk@redhat.com>
 */
class JavaClass implements Comparable<JavaClass> {
  private final String name;
  private final Set<String> dependencies = new TreeSet<String>();

  /**
   * Read class definition from an input stream.
   * 
   * @param filename
   *          the name of the class file (used to determine the class name)
   * @param is
   *          the input stream to read the class file from
   * @throws IOException
   *           if I/O exception occurs while reading from the input stream
   */
  public JavaClass(String filename, InputStream is) throws IOException {
    ClassParser parser = new ClassParser(is, filename);
    org.apache.bcel.classfile.JavaClass clazz = parser.parse();
    ConstantPool cp = clazz.getConstantPool();
    name = clazz.getClassName();

    for (Constant c : cp.getConstantPool()) {
      if (c instanceof ConstantClass) {
        ConstantClass cc = (ConstantClass) c;
        ConstantUtf8 cs = (ConstantUtf8) cp.getConstant(cc.getNameIndex());
        String cn = new String(cs.getBytes());
        if (cn.contains("["))
          continue;
        cn = cn.replaceAll("^\\[L", "");
        cn = cn.replaceAll(";", "");
        cn = cn.replaceAll("/", ".");
        getDependencies().add(cn);
      }
    }
  }

  /**
   * Get the the qualified name of the class.
   * 
   * @return the qualified name of the class.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the the qualified name of the Java package the class belongs to.
   * 
   * @return the qualified name of the Java package the class belongs to.
   */
  public String getPackageName() {
    return name;
  }

  public int compareTo(JavaClass rhs) {
    return name.compareTo(rhs.name);
  }

  Set<String> getDependencies() {
    return dependencies;
  }
}
