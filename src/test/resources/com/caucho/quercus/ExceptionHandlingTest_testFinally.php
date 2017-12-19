<?php

class TestFall {

    public $curPos = 0;
    public $posFinally = -1;
    public $posEnd = -1;

    public function basicRun() {
        $this->curPos++;

        try {
            $this->curPos++;
            throw new Exception("Test");
            $this->curPos++;
        } finally {
            $this->posFinally = $this->curPos;
        }

        $this->posEnd = $this->curPos;
    }

}

$test = new TestFall();
try {
    $test->basicRun();
} catch (Exception $ex) {}

return array("posFinally" => $test->posFinally, "posEnd" => $test->posEnd);