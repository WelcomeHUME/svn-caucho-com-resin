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

package com.caucho.hemp.servlet;

import com.caucho.bam.BamConnection;
import com.caucho.bam.BamStream;
import com.caucho.bam.hmtp.HmtpPacketType;
import com.caucho.bam.hmtp.FromLinkStream;
import com.caucho.bam.BamError;
import com.caucho.hemp.broker.HempBroker;
import java.io.*;
import java.util.logging.*;
import javax.servlet.*;

import com.caucho.hemp.*;
import com.caucho.hessian.io.*;
import com.caucho.bam.BamBroker;
import com.caucho.bam.BamException;
import com.caucho.server.connection.*;
import com.caucho.util.L10N;
import com.caucho.vfs.*;

/**
 * Main protocol handler for the HTTP version of HMTP
 */
public class ServerFromLinkStream extends FromLinkStream
  implements TcpDuplexHandler
{
  private static final L10N L = new L10N(ServerFromLinkStream.class);
  private static final Logger log
    = Logger.getLogger(ServerFromLinkStream.class.getName());
  
  private HempBroker _broker;
  private BamConnection _conn;
  private BamStream _toBroker;

  private Hessian2StreamingInput _in;
  private Hessian2Output _out;

  private BamStream _linkStream;
  private ServerLinkService _linkService;
  private BamStream _linkServiceStream;
  private AuthBrokerStream _authHandler;

  private String _jid;

  public ServerFromLinkStream(HempBroker broker,
			      ServerLinkManager linkManager,
			      InputStream is,
			      OutputStream os,
			      String ipAddress)
  {
    super(is);
	  
    _broker = broker;

    if (log.isLoggable(Level.FINEST)) {
      is = new HessianDebugInputStream(is, log, Level.FINEST);
    }

    _in = new Hessian2StreamingInput(is);

    _linkStream = new ServerToLinkStream(getJid(), os);
    // _authHandler = new AuthBrokerStream(getJid(), _agentStream);
    _linkService = new ServerLinkService(this, _linkStream, linkManager);
    
    _linkServiceStream = new ServerLinkFilter(_linkService.getAgentStream(),
					      ipAddress);
  }

  public String getJid()
  {
    return _jid;
  }

  @Override
  protected BamStream getStream(String to)
  {
    BamStream stream;
    
    if (to == null)
      return _linkServiceStream;
    else if (_conn != null)
      return _toBroker;
    else
      return _authHandler;
  }

  @Override
  protected String getFrom(String from)
  {
    return getJid();
  }
  
  public boolean serviceRead(ReadStream is,
			     TcpDuplexController controller)
    throws IOException
  {
    try {
      if (readPacket()) {
	return true;
      }
      else {
	controller.close();
	return false;
      }
    } catch (Exception e) {
      return false;
    }
  }
  
  public boolean serviceWrite(WriteStream os,
			      TcpDuplexController controller)
    throws IOException
  {
    return false;
  }

  String login(String uid, Object credentials, String resource,
	       String ipAddress)
  {
    if (credentials != null && ! (credentials instanceof String)) {
      throw new BamException(L.l("'{0}' is an unknown credential",
				 credentials));
    }
    
    String password = (String) credentials;
    
    _conn = _broker.getConnection(uid, password, null, ipAddress);
    _conn.setAgentStream(_linkStream);

    _jid = _conn.getJid();
    
    _toBroker = _conn.getBrokerStream();

    return _jid;
  }
  
  /**
   * Handles a message
   */
  public void message(String to,
		      String from,
		      Serializable value)
  {
    _toBroker.message(to, _jid, value);
  }
  
  /**
   * Handles a message
   */
  public void messageError(String to,
			       String from,
			       Serializable value,
			       BamError error)
  {
    _toBroker.messageError(to, _jid, value, error);
  }
  
  /**
   * Handles a get query.
   *
   * The get handler must respond with either
   * a QueryResult or a QueryError 
   */
  public boolean queryGet(long id,
			      String to,
			      String from,
			      Serializable value)
  {
    _toBroker.queryGet(id, to, _jid, value);
    
    return true;
  }
  
  /**
   * Handles a set query.
   *
   * The set handler must respond with either
   * a QueryResult or a QueryError 
   */
  public boolean querySet(long id,
			  String to,
			  String from,
			  Serializable value)
  {
    _toBroker.querySet(id, to, _jid, value);
    
    return true;
  }
  
  /**
   * Handles a query result.
   *
   * The result id will match a pending get or set.
   */
  public void queryResult(long id,
			      String to,
			      String from,
			      Serializable value)
  {
    _toBroker.queryResult(id, to, _jid, value);
  }
  
  /**
   * Handles a query error.
   *
   * The result id will match a pending get or set.
   */
  public void queryError(long id,
			     String to,
			     String from,
			     Serializable value,
			     BamError error)
  {
    _toBroker.queryError(id, to, _jid, value, error);
  }
  
  /**
   * Handles a presence availability packet.
   *
   * If the handler deals with clients, the "from" value should be ignored
   * and replaced by the client's jid.
   */
  public void presence(String to,
			   String from,
			   Serializable data)

  {
    _toBroker.presence(to, _jid, data);
  }
  
  /**
   * Handles a presence unavailability packet.
   *
   * If the handler deals with clients, the "from" value should be ignored
   * and replaced by the client's jid.
   */
  public void presenceUnavailable(String to,
				      String from,
				      Serializable data)
  {
    _toBroker.presenceUnavailable(to, _jid, data);
  }
  
  /**
   * Handles a presence probe from another server
   */
  public void presenceProbe(String to,
			      String from,
			      Serializable data)
  {
    _toBroker.presenceProbe(to, _jid, data);
  }
  
  /**
   * Handles a presence subscribe request from a client
   */
  public void presenceSubscribe(String to,
				    String from,
				    Serializable data)
  {
    _toBroker.presenceSubscribe(to, _jid, data);
  }
  
  /**
   * Handles a presence subscribed result to a client
   */
  public void presenceSubscribed(String to,
				     String from,
				     Serializable data)
  {
    _toBroker.presenceSubscribed(to, _jid, data);
  }
  
  /**
   * Handles a presence unsubscribe request from a client
   */
  public void presenceUnsubscribe(String to,
				      String from,
				      Serializable data)
  {
    _toBroker.presenceUnsubscribe(to, _jid, data);
  }
  
  /**
   * Handles a presence unsubscribed result to a client
   */
  public void presenceUnsubscribed(String to,
				       String from,
				       Serializable data)
  {
    _toBroker.presenceUnsubscribed(to, _jid, data);
  }
  
  /**
   * Handles a presence unsubscribed result to a client
   */
  public void presenceError(String to,
			      String from,
			      Serializable data,
			      BamError error)
  {
    _toBroker.presenceError(to, _jid, data, error);
  }

  public void close()
  {
    Hessian2StreamingInput in = _in;
    _in = null;
    
    Hessian2Output out = _out;
    _out = null;

    if (in != null) {
      try { in.close(); } catch (IOException e) {}
    }

    if (out != null) {
      try { out.close(); } catch (IOException e) {}
    }
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _conn + "]";
  }
}
