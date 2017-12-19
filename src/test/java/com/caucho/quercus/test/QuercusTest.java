
package com.caucho.quercus.test;

import com.caucho.quercus.ExceptionHandlingTest;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.script.QuercusScriptEngine;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.ScriptException;

public class QuercusTest {
    
  protected Value executeAndReturnValue(String snippet) throws ScriptException {
    QuercusScriptEngine qse = new QuercusScriptEngine(true);
    return (Value) qse.eval("<?php " + snippet);
  }
  
  protected Value executeFileAndReturnValue(String suffix) throws IOException, ScriptException {
    QuercusScriptEngine qse = new QuercusScriptEngine(true);
    try (InputStream is = ExceptionHandlingTest.class.getResourceAsStream(getClass().getSimpleName() + "_" + suffix + ".php")) {
      return ((Value) qse.eval(new InputStreamReader(is)));
    }
  }
}
