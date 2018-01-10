package com.caucho.quercus;

import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.script.QuercusScriptEngine;
import com.caucho.quercus.test.QuercusTest;
import javax.script.ScriptException;
import static org.junit.Assert.*;
import org.junit.Test;

public class TypeHintingTest {

  private static final String errorHandler
          = "class ErrorHandler {\n"
          + "   public $error_detected = false;"
          + "   \n"
          + "   public function errorDetected($errorno, $errstr, $errfile, $errline) {\n"
          + "     $this->error_detected = true;\n"
          + "   }\n"
          + "   public function install() {\n"
          + "     set_error_handler(array($this, 'errorDetected'));\n"
          + "   }\n"
          + "}\n"
          + ""
          + "$errorHandler = new ErrorHandler();\n"
          + "$errorHandler->install();\n";

  private static final String demoClasses
          = "// A demo class\n"
          + "class MyClass {\n"
          + "\n"
          + "    /**\n"
          + "     * Eine Testfunktion\n"
          + "     *\n"
          + "     * Der erste Parameter muss ein Objekt des Typs OtherClass sein\n"
          + "     */\n"
          + "    public function test(OtherClass $otherclass) {\n"
          + "//        echo $otherclass->var;\n"
          + "    }\n"
          + "\n"
          + "    /**\n"
          + "     * Eine weitere Testfunktion\n"
          + "     *\n"
          + "     * Der erste Parameter muss ein Array sein\n"
          + "     */\n"
          + "    public function test_array(array $input_array) {\n"
          + "//        print_r($input_array);\n"
          + "    }\n"
          + "\n"
          + "    /**\n"
          + "     * Der erste Parameter muss ein Iterator sein\n"
          + "     */\n"
          + "    public function test_interface(Traversable $iterator) {\n"
          + "//        echo get_class($iterator);\n"
          + "    }\n"
          + "\n"
          + "    /**\n"
          + "     * Der erste Parameter muss ein callable sein\n"
          + "     */\n"
          + "    public function test_callable(callable $callback, $data) {\n"
          + "//        call_user_func($callback, $data);\n"
          + "    }\n"
          + "\n"
          + "    /**\n"
          + "     *  Akzeptiert NULL Werte \n"
          + "     */\n"
          + "    public function test_nullable_stdclass(stdClass $obj = NULL) {\n"
          + "        \n"
          + "    }\n"
          + "\n"
          + "}\n"
          + "\n"
          + "// A second demo class\n"
          + "class OtherClass {\n"
          + "\n"
          + "    public $var = 'Hallo Welt';\n"
          + "\n"
          + "}\n"
          + "$myClass = new MyClass();\n"
          + "$otherClass = new OtherClass();\n";

  @Test
  public void testOtherClassString() throws ScriptException {
    // Fatal Error: Argument 1 must be an object of class OtherClass
    assertTrue(executeAndReportErrorHappend("$myClass->test('hello')"));
  }

  @Test
  public void testOtherClassStdClass() throws ScriptException {
    // Fatal Error: Argument 1 must be an instance of OtherClass
    assertTrue(executeAndReportErrorHappend("$foo = new stdClass; $myClass->test($foo)"));
  }

  @Test
  public void testOtherClassNULL() throws ScriptException {
    // Fatal Error: Argument 1 must not be null
    assertTrue(executeAndReportErrorHappend("$myClass->test(null)"));
  }

  @Test
  public void testOtherClassOtherClass() throws ScriptException {
    // Funktioniert: Gibt Hallo Welt aus
    assertFalse(executeAndReportErrorHappend("$myClass->test($otherClass)"));
  }

  @Test
  public void testArrayString() throws ScriptException {
    // Fatal Error: Argument 1 must be an array
    assertTrue(executeAndReportErrorHappend("$myClass->test_array('a string')"));
  }

  @Test
  public void testArrayArray() throws ScriptException {
    // Funktioniert: Gibt das Array aus
    assertFalse(executeAndReportErrorHappend("$myClass->test_array(array('a', 'b', 'c'))"));
  }

  @Test
  public void testInterfaceImplementation() throws ScriptException {
    // Funktioniert: Gibt das ArrayObject aus
    assertFalse(executeAndReportErrorHappend("$myClass->test_interface(new ArrayObject(array()))"));
  }

  @Test
  public void testCallableCallable() throws ScriptException {
    // Funktioniert: Gibt int(1) aus
    assertFalse(executeAndReportErrorHappend("$myClass->test_callable('var_dump', 1)"));
  }

  @Test
  public void testNullableStdClassNULL() throws ScriptException {
    assertFalse(executeAndReportErrorHappend("$myClass->test_nullable_stdclass(NULL)"));
  }

  @Test
  public void testNullableStdClassStdClass() throws ScriptException {
    assertFalse(executeAndReportErrorHappend("$myClass->test_nullable_stdclass(new stdClass)"));
  }

  @Test
  public void testNullableStdClassArray() throws ScriptException {
    assertTrue(executeAndReportErrorHappend("$myClass->test_nullable_stdclass(array())"));
  }

  private static boolean executeAndReportErrorHappend(String snippet) throws ScriptException {
    QuercusScriptEngine qse = new QuercusScriptEngine();
    String runScript = "<?php\n";
    runScript += errorHandler;
    runScript += demoClasses;
    runScript += snippet;
    runScript += ";\nreturn $errorHandler->error_detected;";
    return ((BooleanValue) qse.eval(runScript)).toBoolean();
  }
}
