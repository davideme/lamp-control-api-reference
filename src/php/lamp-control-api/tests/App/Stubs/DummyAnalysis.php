<?php

namespace Tests\App\Stubs;

use Neomerx\Cors\Contracts\AnalysisResultInterface;

class DummyAnalysis implements AnalysisResultInterface
{
    private $type;
    private $headers;
    public function __construct($type, $headers = [])
    {
        $this->type = $type;
        $this->headers = $headers;
    }
    public function getRequestType(): int
    {
        return $this->type;
    }
    public function getResponseHeaders(): array
    {
        return $this->headers;
    }
    public function getOrigin()
    {
        return null;
    }
    public function getHost()
    {
        return null;
    }
    public function getMethod()
    {
        return null;
    }
    public function getHeaders()
    {
        return [];
    }
    public function getCredentials()
    {
        return null;
    }
    public function getRequest()
    {
        return null;
    }
    public function getResponseStatus()
    {
        return 200;
    }
    public function getResponseBody()
    {
        return null;
    }
    public function getResponseError()
    {
        return null;
    }
}
