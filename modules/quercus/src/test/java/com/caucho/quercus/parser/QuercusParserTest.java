
package com.caucho.quercus.parser;

import com.caucho.quercus.QuercusContext;
import com.caucho.quercus.program.QuercusProgram;
import com.caucho.vfs.NullPath;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

public class QuercusParserTest {
  @Test
  public void testParseFunctionDefitition() throws IOException {
    testParse("<?php function doWork($a) {return a;}");
  }
  
  @Test
  public void testParseFunctionDefititionWithReturn() throws IOException {
    testParse("<?php function doWork($a) : void {return a;}");
  }
  
  @Test
  public void testParseClosureDefinition() throws IOException {
    testParse("<?php $b = function($a) {return a;};");
  }
  
  @Test
  public void testParseClosureDefinitionWithReturn() throws IOException {
    testParse("<?php $b = function($a) : void {return a;};");
  }
  
  @Test
  public void testParseMethodDefitition() throws IOException {
    testParse("<?php class a {function doWork($a) {return a;}}");
  }
  
  @Test
  public void testParseMethodDefititionWithReturn() throws IOException {
    testParse("<?php class a {function doWork($a) : void {return a;}}");
  }
  
  @Test
  public void testParseAbstractMethodDefitition() throws IOException {
    testParse("<?php abstract class a {abstract function doWork($a);}");
  }
  
  @Test
  public void testParseAbstractMethodDefititionWithReturn() throws IOException {
    testParse("<?php abstract class a {abstract function doWork($a) : void;}");
  }
  
  private QuercusProgram testParse(String programm) throws IOException {
    QuercusContext ctx = new QuercusContext();
    QuercusParser parser = new QuercusParser(ctx, new NullPath(""), new StringReader(programm));
    return parser.parse();
  }
}
