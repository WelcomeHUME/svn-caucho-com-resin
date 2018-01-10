package com.caucho.quercus;

import com.caucho.quercus.test.QuercusTest;
import javax.script.ScriptException;
import org.junit.Test;
import static org.junit.Assert.*;

public class NanInfConstantsTest extends QuercusTest {

  @Test
  public void inf() throws ScriptException {
    assertTrue("INF is not detected as infinite", executeAndReturnValue("return is_infinite(INF)").toBoolean());
  }
  
  @Test
  public void nan() throws ScriptException {
    assertTrue("NAN is not detected as not a number", executeAndReturnValue("return is_nan(NAN)").toBoolean());
  }
}
