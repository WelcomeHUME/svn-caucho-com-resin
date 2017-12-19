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
        } catch (Exception $ex) {
            $this->curPos++;
        } finally {
            $this->posFinally = $this->curPos;
        }

        $this->posEnd = $this->curPos;
    }

}

$test = new TestFall();
$test->basicRun();

return array("posFinally" => $test->posFinally, "posEnd" => $test->posEnd);