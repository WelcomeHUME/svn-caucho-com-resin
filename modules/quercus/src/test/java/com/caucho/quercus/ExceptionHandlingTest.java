
package com.caucho.quercus;

import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.test.QuercusTest;
import java.io.IOException;
import javax.script.ScriptException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExceptionHandlingTest extends QuercusTest {
  @Test
  public void testCatchFinally() throws IOException, ScriptException {
    Value result = executeFileAndReturnValue("testCatchFinally");
    assertEquals(3L, result.get(StringValue.create("posFinally")).toLong());
    assertEquals(3L, result.get(StringValue.create("posEnd")).toLong());
  }
  
  @Test
  public void testFinally() throws IOException, ScriptException {
    Value result = executeFileAndReturnValue("testFinally");
    assertEquals(2L, result.get(StringValue.create("posFinally")).toLong());
    assertEquals(-1L, result.get(StringValue.create("posEnd")).toLong());
  }
  
  @Test
  public void testFinallyReturn() throws IOException, ScriptException {
    Value result = executeFileAndReturnValue("testFinallyReturn");
    assertEquals(2L, result.get(StringValue.create("posFinally")).toLong());
    assertEquals(3L, result.get(StringValue.create("posEnd")).toLong());
  }
}
