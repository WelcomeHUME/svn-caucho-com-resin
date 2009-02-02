/*
 * Copyright (c) 1998-2008 Caucho Technology -- all rights reserved
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

package com.caucho.quercus.lib.file;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.EnvCleanup;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.resources.StreamContextResource;
import com.caucho.vfs.HttpPath;
import com.caucho.vfs.HttpStreamWrapper;
import com.caucho.vfs.Path;
import com.caucho.vfs.ReadStream;
import com.caucho.vfs.LockableStream;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.*;

/**
 * Represents a Quercus file open for reading
 */
public class FileInput extends ReadStreamInput
    implements LockableStream, EnvCleanup
{
  private static final Logger log
    = Logger.getLogger(FileInput.class.getName());

  protected Env _env;
  protected Path _path;
  protected ReadStream _is;
  
  private FileInput(Env env, Path path)
    throws IOException
  {
    super(env);
    
    _env = env;

    env.addCleanup(this);
    
    _path = path;

    _is = path.openRead();

    init(_is);
  }
  
  public static FileInput create(Env env,
                                 Path path,
                                 StreamContextResource context)
    throws IOException
  {
    if (path instanceof HttpPath)
      return new HttpInput(env, path, context);
    else
      return new FileInput(env, path);
  }

  /**
   * Returns the path.
   */
  public Path getPath()
  {
    return _path;
  }

  /**
   * Opens a copy.
   */
  public BinaryInput openCopy()
    throws IOException
  {
    return new FileInput(_env, _path);
  }

  /**
   * Returns the number of bytes available to be read, 0 if not known.
   */
  public long getLength()
  {
    return getPath().getLength();
  }

  public long seek(long offset, int whence)
  {
    long position;

    switch (whence) {
      case BinaryStream.SEEK_CUR:
        position = getPosition() + offset;
        break;
      case BinaryStream.SEEK_END:
        position = getLength() + offset;
        break;
      case BinaryStream.SEEK_SET:
      default:
        position = offset;
        break;
    }

    if (! setPosition(position))
      return -1L;
    else
      return position;
  }

  /**
   * Lock the shared advisory lock.
   */
  public boolean lock(boolean shared, boolean block)
  {
    return _is.lock(shared, block);
  }

  /**
   * Unlock the advisory lock.
   */
  public boolean unlock()
  {
    return _is.unlock();
  }

  public Value stat()
  {
    return FileModule.statImpl(_env, getPath());
  }

  public void close()
  {
    _env.removeCleanup(this);

    cleanup();
  }

  /**
   * Implements the EnvCleanup interface.
   */
  public void cleanup()
  {
    super.close();
  }

  /**
   * Converts to a string.
   */
  public String toString()
  {
    return "FileInput[" + getPath() + "]";
  }
  
  static class HttpInput extends FileInput
  {
    private byte []_content;
    
    HttpInput(Env env, Path path, StreamContextResource context)
      throws IOException
    {
      super(env, path);

      if (context != null) {
        Value options = context.getOptions();
        
        if (path.getScheme().equals("http"))
          options = options.get(env.createString("http"));
        else
          options = options.get(env.createString("https"));
        
        HttpStreamWrapper httpStream = (HttpStreamWrapper) _is.getSource();
        
        setOptions(env, httpStream, options);
        
        if (_content != null && _content.length > 0)
          httpStream.write(_content, 0, _content.length, false);
      }
    }
    
    private void setOptions(Env env, HttpStreamWrapper stream, Value options)
      throws IOException
    {
      Iterator<Map.Entry<Value,Value>> iter = options.getIterator(env);
      
      while (iter.hasNext()) {
        Map.Entry<Value,Value> entry = iter.next();
        
        String optionName = entry.getKey().toString();

        if (optionName.equals("method"))
          stream.setMethod(entry.getValue().toString());
        else if (optionName.equals("header")) {
          String optionValue = entry.getValue().toString();
          
          int i = optionValue.indexOf(":");
          
          String name;
          String value;
          
          if (i < 0) {
            name = optionValue;
            value = "";
          }
          else {
            name = optionValue.substring(0, i - 1);
            value = optionValue.substring(i + 1);
          }
          
          stream.setAttribute(name, value);
        }
        else if (optionName.equals("user_agent"))
          stream.setAttribute("User-Agent", entry.getValue().toString());
        else if (optionName.equals("content"))
          _content = entry.getValue().toBinaryValue(env).toBytes();
        else if (optionName.equals("proxy")) {
          env.stub("StreamContextResource::proxy option");
        }
        else if (optionName.equals("request_fulluri")) {
          env.stub("StreamContextResource::request_fulluri option");
        }
        else if (optionName.equals("protocol_version")) {
          double version = entry.getValue().toDouble();
          
          if (version == 1.1) {
          }
          else if (version == 1.0)
            stream.setHttp10();
          else
            env.stub("StreamContextResource::protocol_version " + version);
        }
        else if (optionName.equals("timeout")) {
          long ms = (long) entry.getValue().toDouble() * 1000;
          stream.setSocketTimeout(ms);
        }
        else if (optionName.equals("ignore_errors")) {
          env.stub("ignore_errors::ignore_errors option");
        }
        else {
          env.stub("ignore_errors::" + optionName + " option");
        }
      }
    }
    
    public String toString()
    {
      return "HttpInput[" + getPath() + "]";
    }
    
    @Override
    public boolean isEOF()
    {
      if (_is == null)
        return true;
      
      try {
        return getPosition() > _is.available();
      } catch (IOException e) {
        log.log(Level.FINE, e.toString(), e);

        return true;
      }
    }
  }
}

