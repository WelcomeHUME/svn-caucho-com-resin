
package com.caucho.quercus;

import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.test.QuercusTest;
import java.io.IOException;
import javax.script.ScriptException;
import static org.junit.Assert.*;
import org.junit.Test;

public class EncodingTest extends QuercusTest {

  @Test
  public void testKeepUnicodeEscaping() throws ScriptException {
    assertEquals(7L, executeAndReturnValue("return strlen(\"m\\u00b2\")").toLong());
    assertEquals(7L, executeAndReturnValue("return strlen('m\\u00b2')").toLong());
  }
  
  @Test
  public void testAnotherEncodingProblem() throws ScriptException, IOException {
    Value result = executeFileAndReturnValue("testAnotherEncodingProblem");
    assertTrue(result.get(StringValue.create("literalAccess")).toBoolean());
    assertTrue(result.get(StringValue.create("fileAccess")).toBoolean());
  }
  
  @Test
  public void testReadingFromBinaryFile() throws ScriptException, IOException {
    Value result = executeFileAndReturnValue("testReadingFromBinaryFile");
    assertTrue(result.toBoolean());
  }
}
