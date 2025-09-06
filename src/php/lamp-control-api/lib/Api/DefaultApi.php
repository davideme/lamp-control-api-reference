<?php

namespace OpenAPIServer\Api;

use OpenAPIServer\Model\Error;
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

        // Check if data is null or not an array (empty body returns null)
        if ($data === null || !is_array($data)) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'Request body must be a valid JSON object'
            ];
            $response->getBody()->write(json_encode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Check for required status field
        if (!array_key_exists('status', $data)) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'The request contains invalid parameters or malformed data',
                'details' => 'Missing required field: status'
            ];
            $response->getBody()->write(json_encode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Validate status field type
        if (!is_bool($data['status'])) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'The request contains invalid parameters or malformed data',
                'details' => 'Invalid format for parameter "status": expected boolean'
            ];
            $response->getBody()->write(json_encode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        $lampCreate = new LampCreate();
        $lampCreate->setData($data);
        $lamp = $this->repo->create($lampCreate);
        $response->getBody()->write(json_encode($lamp->getData()));
        return $response->withHeader('Content-Type', 'application/json')->withStatus(201);
    }

    public function listLamps(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $lamps = $this->repo->all();
        // Convert lamp objects to data
        $lampsData = array_map(function ($lamp) {
            return $lamp->getData();
        }, $lamps);

        // Create paginated response structure as per OpenAPI spec
        $paginatedResponse = [
            'data' => $lampsData,
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
        $response->getBody()->write(json_encode($lamp->getData()));
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
        $response->getBody()->write(json_encode($lamp->getData()));
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
