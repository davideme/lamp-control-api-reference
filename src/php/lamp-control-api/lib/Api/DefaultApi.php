<?php

namespace OpenAPIServer\Api;

use OpenAPIServer\Model\Lamp;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use Psr\Http\Message\ResponseInterface;
use Psr\Http\Message\ServerRequestInterface;
use Slim\Psr7\Response;

class DefaultApi extends AbstractDefaultApi
{
    private static array $lamps = [];
    private static int $nextId = 1;

    public function createLamp(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $data = json_decode((string)$request->getBody(), true);
        $lampCreate = new LampCreate();
        $lampCreate->setData($data);
        $lamp = new Lamp();
        $lampId = (string)self::$nextId++;
        $lamp->setData([
            'id' => $lampId,
            'status' => $lampCreate->status ?? false
        ]);
        self::$lamps[$lampId] = $lamp;
        $response->getBody()->write(json_encode($lamp));
        return $response->withHeader('Content-Type', 'application/json')->withStatus(201);
    }

    public function listLamps(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $response->getBody()->write(json_encode(array_values(self::$lamps)));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function getLamp(ServerRequestInterface $request, ResponseInterface $response, string $lampId): ResponseInterface
    {
        $lampId = (int)$lampId;
        if (!isset(self::$lamps[$lampId])) {
            return $response->withStatus(404);
        }
        $response->getBody()->write(json_encode(self::$lamps[$lampId]));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function updateLamp(ServerRequestInterface $request, ResponseInterface $response, string $lampId): ResponseInterface
    {
        if (!isset(self::$lamps[$lampId])) {
            return $response->withStatus(404);
        }
        $data = json_decode((string)$request->getBody(), true);
        $lampUpdate = new LampUpdate();
        $lampUpdate->setData($data);
        $lamp = self::$lamps[$lampId];
        $lampData = $lamp->getData();
        if (isset($lampUpdate->status)) {
            $lampData['status'] = $lampUpdate->status;
        }
        $lamp->setData($lampData);
        self::$lamps[$lampId] = $lamp;
        $response->getBody()->write(json_encode($lamp));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function deleteLamp(ServerRequestInterface $request, ResponseInterface $response, string $lampId): ResponseInterface
    {
        $lampId = (int)$lampId;
        if (!isset(self::$lamps[$lampId])) {
            return $response->withStatus(404);
        }
        unset(self::$lamps[$lampId]);
        return $response->withStatus(204);
    }
}
