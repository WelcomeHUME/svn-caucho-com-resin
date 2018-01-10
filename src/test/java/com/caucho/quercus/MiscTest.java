package com.caucho.quercus;

import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.script.QuercusScriptEngine;
import com.caucho.quercus.test.QuercusTest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import javax.script.ScriptException;
import org.junit.Test;
import static org.junit.Assert.*;

public class MiscTest extends QuercusTest {

  @Test
  public void testGeneratedFileIncludeTest() throws IOException, ScriptException {
    assertEquals("bar", executeFileAndReturnValue("testGeneratedFileIncludeTest").toString());
  }

  @Test
  public void testXMLToArray() throws IOException, ScriptException {
    Value parsedArray = executeFileAndReturnValue("testXMLToArray");
    assertEquals("Attributes were not extracted correctly",
            "documentary",
            parsedArray.get(StringValue.create("movies"))
                    .get(StringValue.create("@attributes"))
                    .get(StringValue.create("type"))
                    .toString());
    assertEquals("XML was not parsed correctly",
            "Mr. Coder",
            parsedArray.get(StringValue.create("movies"))
                    .get(StringValue.create("movie"))
                    .get(StringValue.create("characters"))
                    .get(StringValue.create("character"))
                    .get(1)
                    .get(StringValue.create("name"))
                    .toString());
  }
  
  @Test
  public void testOverrideSystemFunctionInNamespace() throws IOException, ScriptException {
    assertEquals("overriden_method", executeFileAndReturnValue("testOverrideSystemFunctionInNamespace").toString());
  }
  
  @Test
  public void MiscTest_testNoUndefinedIndexNoticeOnNullAccess() throws IOException, ScriptException {
    QuercusScriptEngine qse = new QuercusScriptEngine(true);
    StringWriter sw = new StringWriter();
    qse.getContext().setWriter(sw);
    try (InputStream is = ExceptionHandlingTest.class.getResourceAsStream("MiscTest_testNoUndefinedIndexNoticeOnNullAccess.php")) {
      qse.eval(new InputStreamReader(is));
    }
    
    assertEquals(
            "Warning generated on indexed NULL access",
            "eval::4: Notice: 1 [trigger_error]\n\neval::11: Notice: 2 [trigger_error]",
            sw.toString().trim()
    );
  }
}
