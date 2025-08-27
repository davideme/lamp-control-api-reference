<?php

namespace OpenAPIServer\Api;

use OpenAPIServer\Model\Lamp;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use OpenAPIServer\Repository\LampRepository;
use Psr\Http\Message\ResponseInterface;
use Psr\Http\Message\ServerRequestInterface;
use Slim\Psr7\Response;

class DefaultApi extends AbstractDefaultApi
{
    private LampRepository $repo;

    public function __construct()
    {
        $this->repo = new LampRepository();
    }

    public function createLamp(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $data = json_decode((string)$request->getBody(), true);
        $lampCreate = new LampCreate();
        $lampCreate->setData($data);
        $lamp = $this->repo->create($lampCreate);
        $response->getBody()->write(json_encode($lamp));
        return $response->withHeader('Content-Type', 'application/json')->withStatus(201);
    }

    public function listLamps(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $lamps = $this->repo->all();
        // Create paginated response structure as per OpenAPI spec
        $paginatedResponse = [
            'data' => $lamps,
            'hasMore' => false,  // Since we're not implementing actual pagination yet
            'nextCursor' => null
        ];
        $response->getBody()->write(json_encode($paginatedResponse));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function getLamp(
        ServerRequestInterface $request,
        ResponseInterface $response,
        string $lampId
    ): ResponseInterface {
        $lamp = $this->repo->get($lampId);
        if (!$lamp) {
            return $response->withStatus(404);
        }
        $response->getBody()->write(json_encode($lamp));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function updateLamp(
        ServerRequestInterface $request,
        ResponseInterface $response,
        string $lampId
    ): ResponseInterface {
        $data = json_decode((string)$request->getBody(), true);
        $lampUpdate = new LampUpdate();
        $lampUpdate->setData($data);
        $lamp = $this->repo->update($lampId, $lampUpdate);
        if (!$lamp) {
            return $response->withStatus(404);
        }
        $response->getBody()->write(json_encode($lamp));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function deleteLamp(
        ServerRequestInterface $request,
        ResponseInterface $response,
        string $lampId
    ): ResponseInterface {
        $deleted = $this->repo->delete($lampId);
        if (!$deleted) {
            return $response->withStatus(404);
        }
        return $response->withStatus(204);
    }
}
