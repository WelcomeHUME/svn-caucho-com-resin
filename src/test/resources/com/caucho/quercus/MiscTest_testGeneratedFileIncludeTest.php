<?php

class Test {
	public $foo;

	function runFunction() {
		$filename = "test2_".rand().".php";
		file_put_contents($filename, '<?php echo $this->foo;');
		$this->foo = "bar";
		include $filename;
                unlink($filename);
	}
}

ob_start();
(new Test())->runFunction();
return ob_get_clean();