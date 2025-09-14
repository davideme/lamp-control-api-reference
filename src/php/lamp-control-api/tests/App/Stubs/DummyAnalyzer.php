<?php

namespace Tests\App\Stubs;

use OpenAPIServer\Cors\AnalyzerInterface;
use OpenAPIServer\Cors\AnalysisResultInterface;

class DummyAnalyzer implements AnalyzerInterface
{
    private $type;
    private $headers;
    public function __construct($type, $headers = [])
    {
        $this->type = $type;
        $this->headers = $headers;
    }
    public function analyze(\Psr\Http\Message\RequestInterface $request): AnalysisResultInterface
    {
        return new DummyAnalysis($this->type, $this->headers);
    }
    public function setLogger($logger)
    {
        return $this;
    }
}
