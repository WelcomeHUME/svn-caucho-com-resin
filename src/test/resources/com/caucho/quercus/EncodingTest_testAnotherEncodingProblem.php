<?php

$array = ['Straße' => 'asdf'];
$temp_file = tempnam(sys_get_temp_dir(), 'test');
file_put_contents($temp_file, 'Straße');
$string = file_get_contents($temp_file);
unlink($temp_file);

return array("literalAccess" => array_key_exists('Straße', $array),
      "fileAccess" => array_key_exists($string, $array));
