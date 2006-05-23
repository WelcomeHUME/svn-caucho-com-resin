/*
 * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
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

package com.caucho.quercus.env;

import com.caucho.vfs.WriteStream;

import java.util.IdentityHashMap;
import java.io.IOException;

/**
 * Represents a call to an object's method
 */
public class CallbackObjectMethod extends Callback {
  private final Value _obj;
  private final String _methodName;

  public CallbackObjectMethod(Value obj, String methodName)
  {
    _obj = obj;
    _methodName = methodName.intern();
    // XXX: should be able to cache actual method
  }

  /**
   * Evaluates the callback with no arguments.
   *
   * @param env the calling environment
   */
  public Value call(Env env)
  {
    return _obj.callMethod(env, _methodName);
  }

  /**
   * Evaluates the callback with 1 argument.
   *
   * @param env the calling environment
   */
  public Value call(Env env, Value a1)
  {
    return _obj.callMethod(env, _methodName, a1);
  }

  /**
   * Evaluates the callback with 2 arguments.
   *
   * @param env the calling environment
   */
  public Value call(Env env, Value a1, Value a2)
  {
    return _obj.callMethod(env, _methodName, a1, a2);
  }

  /**
   * Evaluates the callback with 3 arguments.
   *
   * @param env the calling environment
   */
  public Value call(Env env, Value a1, Value a2, Value a3)
  {
    return _obj.callMethod(env, _methodName, a1, a2, a3);
  }

  /**
   * Evaluates the callback with 3 arguments.
   *
   * @param env the calling environment
   */
  public Value call(Env env, Value a1, Value a2, Value a3,
			     Value a4)
  {
    return _obj.callMethod(env, _methodName, a1, a2, a3, a4);
  }

  /**
   * Evaluates the callback with 3 arguments.
   *
   * @param env the calling environment
   */
  public Value call(Env env, Value a1, Value a2, Value a3,
		    Value a4, Value a5)
  {
    return _obj.callMethod(env, _methodName, a1, a2, a3, a4, a5);
  }

  public Value call(Env env, Value []args)
  {
    return _obj.callMethod(env, _methodName, args);
  }

  public void varDumpImpl(Env env,
                          WriteStream out,
                          int depth,
                          IdentityHashMap<Value, String> valueSet)
    throws IOException
  {
    out.print(getClass().getName());
    out.print('[');
    out.print(_methodName);
    out.print(']');
  }
  
  // XXX: just a placeholder, need real implementation here
  public boolean isValid()
  {
    return true;
  }
}

