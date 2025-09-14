<?php

declare(strict_types=1);

namespace Tests\App;

use OpenAPIServer\App\ResponseEmitter;
use PHPUnit\Framework\TestCase;
use Psr\Http\Message\ResponseInterface;
use Psr\Http\Message\ServerRequestInterface;
use Neomerx\Cors\Contracts\AnalyzerInterface;
use Neomerx\Cors\Contracts\AnalysisResultInterface;
use Slim\Middleware\ErrorMiddleware;
use Slim\Psr7\Response;

// Minimal stub for ServerRequestInterface
class DummyServerRequest implements ServerRequestInterface
{
    public function getProtocolVersion()
    {
        return '1.1';
    }
    public function withProtocolVersion($version)
    {
        return $this;
    }
    public function getHeaders()
    {
        return [];
    }
    public function hasHeader($name)
    {
        return false;
    }
    public function getHeader($name)
    {
        return [];
    }
    public function getHeaderLine($name)
    {
        return '';
    }
    public function withHeader($name, $value)
    {
        return $this;
    }
    public function withAddedHeader($name, $value)
    {
        return $this;
    }
    public function withoutHeader($name)
    {
        return $this;
    }
    public function getBody()
    {
        return new \Slim\Psr7\Stream(fopen('php://memory', 'r+'));
    }
    public function withBody(\Psr\Http\Message\StreamInterface $body)
    {
        return $this;
    }
    public function getRequestTarget()
    {
        return '/';
    }
    public function withRequestTarget($requestTarget)
    {
        return $this;
    }
    public function getMethod()
    {
        return 'GET';
    }
    public function withMethod($method)
    {
        return $this;
    }
    public function getUri()
    {
        return null;
    }
    public function withUri(\Psr\Http\Message\UriInterface $uri, $preserveHost = false)
    {
        return $this;
    }
    public function getServerParams()
    {
        return [];
    }
    public function getCookieParams()
    {
        return [];
    }
    public function withCookieParams(array $cookies)
    {
        return $this;
    }
    public function getQueryParams()
    {
        return [];
    }
    public function withQueryParams(array $query)
    {
        return $this;
    }
    public function getUploadedFiles()
    {
        return [];
    }
    public function withUploadedFiles(array $uploadedFiles)
    {
        return $this;
    }
    public function getParsedBody()
    {
        return null;
    }
    public function withParsedBody($data)
    {
        return $this;
    }
    public function getAttributes()
    {
        return [];
    }
    public function getAttribute($name, $default = null)
    {
        return $default;
    }
    public function withAttribute($name, $value)
    {
        return $this;
    }
    public function withoutAttribute($name)
    {
        return $this;
    }
}

// Minimal stub for ErrorMiddleware
class DummyErrorMiddleware extends ErrorMiddleware
{
    public function __construct()
    {
    }
    public function handleException(ServerRequestInterface $request, \Throwable $exception): ResponseInterface
    {
        return new Response();
    }
}

// Minimal stub for AnalyzerInterface
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

class ResponseEmitterTest extends TestCase
{
    public function testEmitCoversAllBranches(): void
    {
        $response = new Response();
        $request = new DummyServerRequest();
        $errorMiddleware = new DummyErrorMiddleware();

        // Test each CORS error type
        $errorTypes = [
            AnalysisResultInterface::ERR_NO_HOST_HEADER,
            AnalysisResultInterface::ERR_ORIGIN_NOT_ALLOWED,
            AnalysisResultInterface::ERR_METHOD_NOT_SUPPORTED,
            AnalysisResultInterface::ERR_HEADERS_NOT_SUPPORTED,
        ];
        foreach ($errorTypes as $type) {
            $analyzer = new DummyAnalyzer($type);
            $emitter = new ResponseEmitter();
            $emitter->setRequest($request)
                ->setErrorMiddleware($errorMiddleware)
                ->setAnalyzer($analyzer);
            ob_start();
            $emitter->emit($response);
            ob_end_clean();
        }

        // Test TYPE_REQUEST_OUT_OF_CORS_SCOPE (no error)
        $analyzer = new DummyAnalyzer(AnalysisResultInterface::TYPE_REQUEST_OUT_OF_CORS_SCOPE);
        $emitter = new ResponseEmitter();
        $emitter->setRequest($request)
            ->setErrorMiddleware($errorMiddleware)
            ->setAnalyzer($analyzer);
        ob_start();
        $emitter->emit($response);
        ob_end_clean();

        // Test TYPE_PRE_FLIGHT_REQUEST and default (actual CORS request)
        $analyzer = new DummyAnalyzer(AnalysisResultInterface::TYPE_PRE_FLIGHT_REQUEST, ['Access-Control-Allow-Origin' => '*']);
        $emitter = new ResponseEmitter();
        $emitter->setRequest($request)
            ->setErrorMiddleware($errorMiddleware)
            ->setAnalyzer($analyzer);
        ob_start();
        $emitter->emit($response);
        ob_end_clean();
    }
}
