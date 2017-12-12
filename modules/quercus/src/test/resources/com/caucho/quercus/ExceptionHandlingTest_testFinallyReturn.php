<?php

class TestFall2 {

    public $curPos = 0;
    public $posFinally = -1;
    public $posEnd = -1;

    public function basicRun() {
        $this->basicRun2();
        $this->posEnd = $this->curPos;
    }

    public function basicRun2() {
        $this->curPos++;

        try {
            $this->curPos++;
            return;
        } finally {
            $this->posFinally = $this->curPos;
            $this->curPos++;
        }
    }

}

$test = new TestFall2();
try {
    $test->basicRun();
} catch (Exception $ex) {}

return array("posFinally" => $test->posFinally, "posEnd" => $test->posEnd);