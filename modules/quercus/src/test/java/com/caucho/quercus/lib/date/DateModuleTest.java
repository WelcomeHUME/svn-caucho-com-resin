
package com.caucho.quercus.lib.date;

import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.LongValue;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.script.QuercusScriptEngine;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.script.ScriptException;
import org.junit.Test;
import static org.junit.Assert.*;


public class DateModuleTest {
  private static int now;
  
  @Test
  public void testRFormat() throws ScriptException {
    // See http://bugs.caucho.com/view.php?id=4626 for details
    // %R and %r Modifier were missing from formats
    long currentTS_S = System.currentTimeMillis() / 1000L;
    long currentTS_MS = currentTS_S * 1000L;
    QuercusScriptEngine qse = new QuercusScriptEngine();
    Object formattedStringValue = qse.eval("<?php return strftime('%R', " + currentTS_S + ")?>");
    assertNotNull(formattedStringValue);
    String formattedString = formattedStringValue.toString();
    assertEquals(
            new SimpleDateFormat("HH:mm").format(new Date(currentTS_MS)),
            formattedString);
  }
  
  @Test
  public void testrFormat() throws ScriptException {
    // See http://bugs.caucho.com/view.php?id=4626 for details
    // %R and %r Modifier were missing from formats
    long currentTS_S = System.currentTimeMillis() / 1000L;
    long currentTS_MS = currentTS_S * 1000L;
    QuercusScriptEngine qse = new QuercusScriptEngine();
    Object formattedStringValue = qse.eval("<?php return strftime('%r', " + currentTS_S + ")?>");
    assertNotNull(formattedStringValue);
    String formattedString = formattedStringValue.toString();
    assertEquals(
            new SimpleDateFormat("hh:mm:ss a").format(new Date(currentTS_MS)),
            formattedString);
  }
  
  @Test
  public void testGmmktime() throws ScriptException {
    // This timestamp is the unixtimestamp for 2016-06-16T17:49:14+00:00: 1466099354
    QuercusScriptEngine qse = new QuercusScriptEngine();
    Object gmmktimeValue = qse.eval("<?php return gmmktime(17, 49, 14, 6, 16, 2016); ?>");
    assertNotNull(gmmktimeValue);
    assertEquals(1466099354L, ((LongValue) gmmktimeValue).toLong());
  }
  
  @Test
  public void testMktimeSpill() throws ScriptException {
    long[][] testArray = new long[][] {
      {22, 21, 1, 4, 21, 2016}, // Simple case
      {30, 0, 0, 4, 21, 2016},  // Rollover hour
      {-1, 0, 0, 4, 21, 2016},  // Negative hour
      {-25, 0, 0, 4, 21, 2016}, // Negative hour > 24
      {2, 67, 0, 4, 21, 2016},  // Rollover minute
      {2, -5, 0, 4, 21, 2016},  // Negative minute
      {2, -125, 0, 4, 21, 2016},// Negative minute > 120
      {2, 0, 65, 4, 21, 2016},  // Rollover second
      {2, 1, -30, 4, 21, 2016}, // Negative minute
      {2, 1, -150, 4, 21, 2016},// Negative minute > 120
      {2, 0, 0, 4, 31, 2016},   // Rollover day
      {2, 0, 0, 4, -1, 2016},   // Negative day
      {2, 0, 0, 4, -56, 2016},  // Negative day > 30
      {2, 0, 0, 13, 4, 2016},   // Rollover month
      {2, 0, 0, -1, 4, 2016},   // Negative month
      {2, 0, 0, -13, 4, 2016}   // Negative month > 12
    };
    
    String[] expectedResults = new String[]{
      "2016-04-21 22:21:01",
      "2016-04-22 06:00:00",
      "2016-04-20 23:00:00",
      "2016-04-19 23:00:00",
      "2016-04-21 03:07:00",
      "2016-04-21 01:55:00",
      "2016-04-20 23:55:00",
      "2016-04-21 02:01:05",
      "2016-04-21 02:00:30",
      "2016-04-21 01:58:30",
      "2016-05-01 02:00:00",
      "2016-03-30 02:00:00",
      "2016-02-04 02:00:00",
      "2017-01-04 02:00:00",
      "2015-11-04 02:00:00",
      "2014-11-04 02:00:00"
    };

    QuercusScriptEngine qse = new QuercusScriptEngine();
    for (int i = 0; i < testArray.length; i++) {
      Object formattedStringValue = qse.eval(String.format(
              "<?php $mktime = mktime(%d, %d, %d, %d, %d, %d); return strftime('%%Y-%%m-%%d %%H:%%M:%%S', $mktime);?>",
              testArray[i][0],
              testArray[i][1],
              testArray[i][2],
              testArray[i][3],
              testArray[i][4],
              testArray[i][5]
      ));
      assertEquals(expectedResults[i], formattedStringValue.toString());
    }
  }
}
