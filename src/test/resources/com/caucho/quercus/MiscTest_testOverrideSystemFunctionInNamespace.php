<?php
namespace Test {
	function file_exists($name) {
		return 'overriden_method';
	}
	class Test {
		public function test() {
			return file_exists("name");
		}
	}
}

namespace {
	$test = new Test\Test();
	return $test->test();
}