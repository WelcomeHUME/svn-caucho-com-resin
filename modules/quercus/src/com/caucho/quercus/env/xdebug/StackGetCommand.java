package com.caucho.quercus.env.xdebug;

import java.util.Map;

import com.caucho.quercus.Location;

public class StackGetCommand extends XdebugCommand
{
	
	@Override
  protected XdebugResponse getInternalResponse(String commandName, Map<String, String> parameters,
      String transactionId, XdebugConnection conn) {
		StringBuilder response = new StringBuilder();
		response.append("<response xmlns=\"urn:debugger_protocol_v1\" xmlns:xdebug=\"http://xdebug.org/dbgp/xdebug\" command=\"stack_get\" transaction_id=\"" + transactionId + "\">");
		int level = 0;
		for (Location location : conn.getEnv().getStackTraceAsLocations()) {
			if (location != null) {
				response.append("<stack where=\"{main}\" level=\"");
				response.append(level);
				response.append("\" type=\"file\" filename=\"");
				response.append(conn.getFileURI(location.getFileName()));
				response.append("\" lineno=\"");
				response.append(location.getLineNumber());
				response.append("\"></stack>"); 
			}
			level++;
		}
		response.append("</response>");
	  return new XdebugResponse(null, response.toString(), transactionId);
  }

}
