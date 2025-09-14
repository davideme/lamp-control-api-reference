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
use Tests\App\Stubs\DummyServerRequest;
use Tests\App\Stubs\DummyErrorMiddleware;
use Tests\App\Stubs\DummyAnalyzer;
use Tests\App\Stubs\DummyAnalysis;

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
        $analyzer = new DummyAnalyzer(
            AnalysisResultInterface::TYPE_PRE_FLIGHT_REQUEST,
            [
                'Access-Control-Allow-Origin' => '*'
            ]
        );
        $emitter = new ResponseEmitter();
        $emitter->setRequest($request)
            ->setErrorMiddleware($errorMiddleware)
            ->setAnalyzer($analyzer);
        ob_start();
        $emitter->emit($response);
        ob_end_clean();
    }
}
