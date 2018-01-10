<?php
 $testString = '{"äöüß":"äöüß"}';
 $compressed = gzcompress($testString, 9);
 $uncompressed = gzuncompress($compressed);
 return $testString === $uncompressed;