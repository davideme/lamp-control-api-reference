<?php

namespace Tests\App\Stubs;

use OpenAPIServer\Middleware\ErrorMiddleware;
use Psr\Http\Message\ServerRequestInterface;
use Psr\Http\Message\ResponseInterface;
use OpenAPIServer\Response\Response;

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
