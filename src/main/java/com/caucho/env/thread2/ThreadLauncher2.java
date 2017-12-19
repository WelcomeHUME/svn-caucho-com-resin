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

package com.caucho.env.thread2;

import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

import com.caucho.inject.Module;

@Module
class ThreadLauncher2 extends AbstractThreadLauncher2 {
  private static final Logger log
    = Logger.getLogger(ThreadLauncher2.class.getName());
  
  public static final String THREAD_FULL_EVENT
    = "caucho.thread.pool.full";
  public static final String THREAD_CREATE_THROTTLE_EVENT
    = "caucho.thread.pool.throttle";
  
  private final ThreadPool2 _pool;

  ThreadLauncher2(ThreadPool2 pool)
  {
    super(ClassLoader.getSystemClassLoader());
    
    _pool = pool;
  }
  
  @Override
  public final boolean isPermanent()
  {
    return true;
  }

  @Override
  protected void startWorkerThread()
  {
    Thread thread = new Thread(this);
    thread.setDaemon(true);
    thread.setName("resin-thread-launcher2");
    thread.start();
  }
  
  @Override
  protected long getCurrentTimeActual()
  {
    return System.currentTimeMillis();
  }
  
  @Override
  protected void launchChildThread(int id)
  {
      ResinThread2 poolThread = new ResinThread2(id, _pool, this);
      poolThread.start();
  }
  
  @Override
  protected void unpark(Thread thread)
  {
    LockSupport.unpark(thread);
  }
  
  //
  // event handling
  
  @Override
  protected void onThreadMax()
  {
  }

  @Override
  protected void onThrottle(String msg)
  {
    log.warning(msg);
  }
  
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _pool + "]";
  }
}
