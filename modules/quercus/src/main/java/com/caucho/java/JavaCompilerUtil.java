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

import com.caucho.server.util.CauchoSystem;
import com.caucho.util.CharBuffer;

/**
 * Compiles Java source, returning the loaded class.
 */
public class JavaCompilerUtil {

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

}
