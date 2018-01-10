<?php
error_reporting(E_ALL|E_STRICT);

trigger_error("1");

$test = null;
$test2 = [
		'test' => $test['test']
];

trigger_error("2");