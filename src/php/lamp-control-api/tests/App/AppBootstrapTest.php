<?php

declare(strict_types=1);

namespace Tests\App;

use DI\ContainerBuilder;
use PHPUnit\Framework\TestCase;
use Slim\App;
use Slim\Psr7\Factory\ServerRequestFactory;
use Slim\Psr7\Response;
use OpenAPIServer\App\RegisterDependencies;
use OpenAPIServer\App\RegisterRoutes;
use OpenAPIServer\App\RegisterMiddlewares;

class AppBootstrapTest extends TestCase
{
    public function testAppBootstrapsAndHandlesRoute(): void
    {
        $containerBuilder = new ContainerBuilder();
        (new RegisterDependencies())->__invoke($containerBuilder);
        $container = $containerBuilder->build();
        $responseFactory = $container->get(\Psr\Http\Message\ResponseFactoryInterface::class);
        $app = new App($responseFactory, $container);
        (new RegisterMiddlewares())->__invoke($app);
        (new RegisterRoutes())->__invoke($app);

        $requestFactory = new ServerRequestFactory();
        $request = $requestFactory->createServerRequest('POST', '/v1/lamps')
            ->withHeader('Content-Type', 'application/json');
        $request->getBody()->write('{"status":true}');
        $request->getBody()->rewind();
        $response = new Response();

        $response = $app->handle($request);
        $this->assertEquals(201, $response->getStatusCode());
        $body = json_decode((string)$response->getBody(), true);
        $this->assertIsArray($body);
        $this->assertArrayHasKey('id', $body);
        $this->assertArrayHasKey('status', $body);
    }
}
