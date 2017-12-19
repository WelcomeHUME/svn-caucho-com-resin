
package com.caucho.quercus;

import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.script.QuercusScriptEngine;
import com.caucho.quercus.test.QuercusTest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.ScriptException;
import static org.junit.Assert.*;
import org.junit.Test;

public class StringTest extends QuercusTest {
  @Test
  public void testJsonEncodingBinaryBuilderValue() throws ScriptException, IOException {
    assertTrue(executeFileAndReturnValue("testJsonEncodingBinaryBuilderValue").toBoolean());
  }
  
  @Test
  public void testStrictEquality() throws ScriptException, IOException {
    assertTrue(executeFileAndReturnValue("testStrictEquality").toBoolean());
  }
  
  @Test
  public void testConcatingBinaryStringWithUnicodeString()  throws ScriptException, IOException {
    Value av = executeFileAndReturnValue("testConcatingBinaryStringWithUnicodeString");
    assertTrue("PDF broke during concatenation with UTF-8 string", av.get(StringValue.create("pdfStringConcatResult")).toBoolean());
    assertTrue("PDF broke during concatenation with UTF-8 string", av.get(StringValue.create("emptyStringConcatResult")).toBoolean());
    assertEquals("strlen must return 12", 12L, av.get(StringValue.create("emptyStringConcatLength")).toLong());
    assertTrue("comparison between UnicodeBuilderValue and BinaryBuilderValue failed 1", av.get(StringValue.create("unicodeBinaryComparison1")).toBoolean());
    assertTrue("comparison between UnicodeBuilderValue and BinaryBuilderValue failed 2", av.get(StringValue.create("unicodeBinaryComparison2")).toBoolean());
    assertTrue("characters not found in length map (binary string)", av.get(StringValue.create("lengthMapBinary")).toBoolean());
    assertTrue("characters not found in length map (unicode string)", av.get(StringValue.create("lengthMapUnicode")).toBoolean());
  }
  
  @Test
  public void testScanfWithCharacterSets() throws ScriptException {
    Value v = executeAndReturnValue("sscanf ( \"J7\", '%[_Z-A]%d', $r1, $r2); return array($r1, $r2)");
    assertEquals("J", v.get(0).toString());
    assertEquals("7", v.get(1).toString());
    v = executeAndReturnValue("sscanf ( \"_7\", '%[_-]%d', $r1, $r2 ); return array($r1, $r2)");
    assertEquals("_", v.get(0).toString());
    assertEquals("7", v.get(1).toString());
    v = executeAndReturnValue("sscanf ( \"-7\", '%[-]%[0-9]', $r1, $r2 ); return array($r1, $r2)");
    assertEquals("-", v.get(0).toString());
    assertEquals("7", v.get(1).toString());
  }
}
