/*
 * Copyright (c) 1998-2012 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.java;

import com.caucho.i18n.CharacterEncoding;
import com.caucho.server.util.CauchoSystem;
import com.caucho.util.CharBuffer;
import com.caucho.util.L10N;
import com.caucho.vfs.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Compiles Java source, returning the loaded class.
 */
public class JavaCompilerUtil {
  static final L10N L = new L10N(JavaCompilerUtil.class);
  static final Logger log
    = Logger.getLogger(JavaCompilerUtil.class.getName());

  private static final Object LOCK = new Object();

  // Parent class loader.  Used to grab the classpath.
  private ClassLoader _loader;

  // Executable name of the compiler
  private String _compiler;

  private String _sourceExt = ".java";

  private Path _classDir;
  private Path _sourceDir;

  private boolean _compileParent = true;

  private String _extraClassPath;
  private String _classPath;

  protected String _charEncoding;
  protected ArrayList<String> _args;

  private int _maxBatch = 64;
  private long _startTimeout = 10 * 1000L;
  private long _maxCompileTime = 120 * 1000L;

  private JavaCompilerUtil()
  {
    String encoding = CharacterEncoding.getLocalEncoding();
    String javaEncoding = Encoding.getJavaName(encoding);

    if (javaEncoding == null || javaEncoding.equals("ISO8859_1"))
      javaEncoding = null;
    
    _charEncoding = javaEncoding;
  }

  /**
   * Creates a new compiler.
   */
  public static JavaCompilerUtil create()
  {
    return create(Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates a new compiler.
   *
   * @param loader the parent class loader for the compiler.
   */
  public static JavaCompilerUtil create(ClassLoader loader)
  {
    return null;
  }

  /**
   * Sets the class loader used to load the compiled class and to grab
   * the classpath from.
   */
  public void setClassLoader(ClassLoader loader)
  {
    _loader = loader;
  }

  /**
   * Sets the class loader used to load the compiled class and to grab
   * the classpath from.
   */
  public ClassLoader getClassLoader()
  {
    return _loader;
  }

  /**
   * Sets the compiler name, e.g. jikes.
   */
  public void setCompiler(String compiler)
  {
    _compiler = compiler;
  }

  /**
   * Gets the compiler name, e.g. jikes.
   */
  public String getCompiler()
  {
    if (_compiler == null)
      _compiler = "javac";

    return _compiler;
  }

  /**
   * Sets the directory where compiled classes go.
   *
   * @param path representing the class dir.
   */
  public void setClassDir(Path path)
  {
    try {
      path.mkdirs();
    } catch (IOException e) {
    }

    _classDir = path;
  }

  /**
   * Returns the directory where compiled classes go.
   */
  Path getClassDir()
  {
    if (_classDir != null)
      return _classDir;
    else
      return CauchoSystem.getWorkPath();
  }

  /**
   * Returns the path to the class directory.
   */
  String getClassDirName()
  {
    return getClassDir().getNativePath();
  }

  /**
   * Sets the directory the java source comes from.
   *
   * @param path representing the source dir.
   */
  public void setSourceDir(Path path)
  {
    _sourceDir = path;
  }

  /**
   * Returns the directory where compiled classes go.
   */
  public Path getSourceDir()
  {
    if (_sourceDir != null)
      return _sourceDir;
    else
      return getClassDir();
  }

  /**
   * Returns the path to the class directory.
   */
  String getSourceDirName()
  {
    return getSourceDir().getNativePath();
  }

  /**
   * Sets the source extension.
   */
  public void setSourceExtension(String ext)
  {
    _sourceExt = ext;
  }

  /**
   * Gets the source extension.
   */
  public String getSourceExtension()
  {
    return _sourceExt;
  }

  /**
   * Sets the class path for compilation.  Normally, the classpath from
   * the class loader will be sufficient.
   */
  public void setClassPath(String classPath)
  {
    _classPath = classPath;
  }

  /**
   * Sets an extra class path for compilation.
   */
  public void setExtraClassPath(String classPath)
  {
    _extraClassPath = classPath;
  }

  public void setCompileParent(boolean compileParent)
  {
    _compileParent = compileParent;
  }

  /**
   * Sets any additional arguments for the compiler.
   */
  public void setArgs(String argString)
  {
    try {
      if (argString != null) {
        String []args = Pattern.compile("[\\s,]+").split(argString);

        _args = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
          if (! args[i].equals(""))
            _args.add(args[i]);
        }
      }
    } catch (Exception e) {
      log.log(Level.WARNING, e.toString(), e);
    }
  }

  /**
   * Returns the ArrayList of arguments.
   */
  public ArrayList<String> getArgs()
  {
    return _args;
  }

  /**
   * Sets the Java encoding for the compiler.
   */
  public void setEncoding(String encoding)
  {
    _charEncoding = encoding;

    String javaEncoding = Encoding.getJavaName(encoding);

    if ("ISO8859_1".equals(javaEncoding))
      _charEncoding = null;
  }

  /**
   * Returns the encoding.
   */
  public String getEncoding()
  {
    return _charEncoding;
  }

  /**
   * Returns the thread spawn time for an external compilation.
   */
  public long getStartTimeout()
  {
    return _startTimeout;
  }

  /**
   * Sets the thread spawn time for an external compilation.
   */
  public void setStartTimeout(long startTimeout)
  {
    _startTimeout = startTimeout;
  }

  /**
   * Returns the maximum time allowed for an external compilation.
   */
  public long getMaxCompileTime()
  {
    return _maxCompileTime;
  }

  /**
   * Sets the maximum time allowed for an external compilation.
   */
  public void setMaxCompileTime(long maxCompileTime)
  {
    _maxCompileTime = maxCompileTime;
  }

  /**
   * Sets the maximum time allowed for an external compilation.
   */
  public void setMaxBatch(int maxBatch)
  {
    _maxBatch = maxBatch;
  }

  /**
   * Mangles the path into a valid Java class name.
   */
  public static String mangleName(String name)
  {
    // canonicalize to lower case for the tag prefix loading
    int p = name.toLowerCase().indexOf("web-inf");
    if (p >= 0) {
      int len = "web-inf".length();
      
      name = name.substring(0, p) + "web-inf" + name.substring(p + len);
    }
    
    boolean toLower = CauchoSystem.isCaseInsensitive();

    CharBuffer cb = new CharBuffer();
    cb.append("_");

    for (int i = 0; i < name.length(); i++) {
      char ch = name.charAt(i);

      if (ch == '/' || ch == CauchoSystem.getPathSeparatorChar()) {
        if (i == 0) {
        }
        else if (cb.charAt(cb.length() - 1) != '.' &&
                 (i + 1 < name.length() && name.charAt(i + 1) != '/'))
          cb.append("._");
      }
      else if (ch == '.')
        cb.append("__");
      else if (ch == '_')
        cb.append("_0");
      else if (Character.isJavaIdentifierPart(ch))
        cb.append(toLower ? Character.toLowerCase(ch) : ch);
      else if (ch <= 256)
        cb.append("_2" + encodeHex(ch >> 4) + encodeHex(ch));
      else
        cb.append("_4" + encodeHex(ch >> 12) + encodeHex(ch >> 8) +
                  encodeHex(ch >> 4) + encodeHex(ch));
    }

    if (cb.length() == 0)
      cb.append("_z");

    return cb.toString();
  }

  private static char encodeHex(int i)
  {
    i &= 0xf;

    if (i < 10)
      return (char) (i + '0');
    else
      return (char) (i - 10 + 'a');
  }

  public void setArgs(ArrayList<String> args)
  {
    if (args == null)
      return;
    if (_args == null)
      _args = new ArrayList<String>();

    _args.addAll(args);
  }
}
