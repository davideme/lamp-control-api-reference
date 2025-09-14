<?php

namespace OpenAPIServer\Api;

use OpenAPIServer\Mapper\LampMapper;
use OpenAPIServer\Model\Error;
use OpenAPIServer\Model\Lamp;
use OpenAPIServer\Model\LampCreate;
use OpenAPIServer\Model\LampUpdate;
use OpenAPIServer\Repository\LampRepository;
use OpenAPIServer\Service\LampService;
use Psr\Http\Message\ResponseInterface;
use Psr\Http\Message\ServerRequestInterface;
use Slim\Psr7\Response;

class DefaultApi extends AbstractDefaultApi
{
    private LampService $service;

    public function __construct(?LampRepository $repository = null)
    {
        if ($repository === null) {
            $repository = new LampRepository();
        }
        $mapper = new LampMapper();
        $this->service = new LampService($repository, $mapper);
    }

    public function createLamp(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $data = json_decode((string)$request->getBody(), true);

        // Validate JSON parsing
        if (json_last_error() !== JSON_ERROR_NONE) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'Invalid JSON format',
                'details' => json_last_error_msg()
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Check if data is null or not an array (empty body returns null)
        if ($data === null || !is_array($data)) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'Request body must be a valid JSON object'
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Check if it's an indexed array (not an associative array/object)
        if (!empty($data) && array_keys($data) === range(0, count($data) - 1)) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'The request contains invalid parameters or malformed data',
                'details' => 'Request body must be a JSON object, not an array'
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Validate field names - reject empty string keys or invalid field names
        foreach (array_keys($data) as $fieldName) {
            if ($fieldName === '') {
                $errorData = [
                    'error' => 'INVALID_ARGUMENT',
                    'message' => 'The request contains invalid parameters or malformed data',
                    'details' => 'Field names cannot be empty strings'
                ];
                $response->getBody()->write($this->safeJsonEncode($errorData));
                return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
            }

            // Only allow the 'status' field as per LampCreate schema
            if ($fieldName !== 'status') {
                $errorData = [
                    'error' => 'INVALID_ARGUMENT',
                    'message' => 'The request contains invalid parameters or malformed data',
                    'details' => 'Unknown field: ' . $fieldName
                ];
                $response->getBody()->write($this->safeJsonEncode($errorData));
                return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
            }
        }

        // Check for required status field
        if (!array_key_exists('status', $data)) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'The request contains invalid parameters or malformed data',
                'details' => 'Missing required field: status'
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Validate status field type
        if (!is_bool($data['status'])) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'The request contains invalid parameters or malformed data',
                'details' => 'Invalid format for parameter "status": expected boolean'
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        $lampCreate = new LampCreate();
        $lampCreate->setData($data);
        $lamp = $this->service->create($lampCreate);
        $response->getBody()->write($this->safeJsonEncode($lamp->getData()));
        return $response->withHeader('Content-Type', 'application/json')->withStatus(201);
    }

    public function listLamps(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $lamps = $this->service->all();
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
        $response->getBody()->write($this->safeJsonEncode($paginatedResponse));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function getLamp(
        ServerRequestInterface $request,
        ResponseInterface $response,
        string $lampId
    ): ResponseInterface {
        $lamp = $this->service->get($lampId);
        if (!$lamp) {
            return $response->withStatus(404);
        }
        $response->getBody()->write($this->safeJsonEncode($lamp->getData()));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function updateLamp(
        ServerRequestInterface $request,
        ResponseInterface $response,
        string $lampId
    ): ResponseInterface {
        $data = json_decode((string)$request->getBody(), true);

        // Validate JSON parsing
        if (json_last_error() !== JSON_ERROR_NONE) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'Invalid JSON format',
                'details' => json_last_error_msg()
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Check if data is null or not an array (empty body returns null)
        if ($data === null || !is_array($data)) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'Request body must be a valid JSON object'
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Check if it's an indexed array (not an associative array/object)
        if ($data !== [] && array_keys($data) === range(0, count($data) - 1)) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'The request contains invalid parameters or malformed data',
                'details' => 'Request body must be a JSON object, not an array'
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Validate field names - reject empty string keys or invalid field names
        foreach (array_keys($data) as $fieldName) {
            if ($fieldName === '') {
                $errorData = [
                    'error' => 'INVALID_ARGUMENT',
                    'message' => 'The request contains invalid parameters or malformed data',
                    'details' => 'Field names cannot be empty strings'
                ];
                $response->getBody()->write($this->safeJsonEncode($errorData));
                return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
            }

            // Only allow the 'status' field as per LampUpdate schema
            if ($fieldName !== 'status') {
                $errorData = [
                    'error' => 'INVALID_ARGUMENT',
                    'message' => 'The request contains invalid parameters or malformed data',
                    'details' => 'Unknown field: ' . $fieldName
                ];
                $response->getBody()->write($this->safeJsonEncode($errorData));
                return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
            }
        }

        // Check for required status field
        if (!array_key_exists('status', $data)) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'The request contains invalid parameters or malformed data',
                'details' => 'Missing required field: status'
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        // Validate status field type
        if (!is_bool($data['status'])) {
            $errorData = [
                'error' => 'INVALID_ARGUMENT',
                'message' => 'The request contains invalid parameters or malformed data',
                'details' => 'Invalid format for parameter "status": expected boolean'
            ];
            $response->getBody()->write($this->safeJsonEncode($errorData));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        $lampUpdate = new LampUpdate();
        $lampUpdate->setData($data);
        $lamp = $this->service->update($lampId, $lampUpdate);
        if (!$lamp) {
            return $response->withStatus(404);
        }
        $response->getBody()->write($this->safeJsonEncode($lamp->getData()));
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function deleteLamp(
        ServerRequestInterface $request,
        ResponseInterface $response,
        string $lampId
    ): ResponseInterface {
        $deleted = $this->service->delete($lampId);
        if (!$deleted) {
            // Return 404 with empty body as per OpenAPI spec for lamp not found
            return $response->withStatus(404);
        }
        return $response->withStatus(204);
    }

    /**
     * Helper method to safely encode JSON and handle potential failures
     *
     * @param mixed $data The data to encode
     * @return string The JSON string or fallback error message
     */
    private function safeJsonEncode($data): string
    {
        $json = json_encode($data);
        return $json !== false ? $json : '{"error":"JSON_ENCODE_ERROR"}';
    }
}
