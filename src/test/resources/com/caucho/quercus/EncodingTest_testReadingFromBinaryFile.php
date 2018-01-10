<?php

$tmpfname1 = tempnam("", "UTF8Reader-problems");
$tmpfname2 = tempnam("", "UTF8Reader-problems");

$string = "äöüß";
file_put_contents($tmpfname1, $string);
$read1 = file_get_contents($tmpfname1);
file_put_contents($tmpfname2, $read1);
$read2 = file_get_contents($tmpfname2);
unlink($tmpfname1);
unlink($tmpfname2);
return $string == $read2;