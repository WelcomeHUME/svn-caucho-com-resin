
package com.caucho.quercus;

import com.caucho.quercus.test.QuercusTest;
import java.io.IOException;
import javax.script.ScriptException;
import static org.junit.Assert.*;
import org.junit.Test;

public class ReflectionTest extends QuercusTest {

  @Test
  public void testReflectivePropertyTest1() throws IOException, ScriptException {
    assertEquals(
            "parent value of ParentClass should be changed, not of ChildClass", 
            "parent2", 
            executeFileAndReturnValue("testReflectivePropertyTest1").toString()
    );
  }
}
