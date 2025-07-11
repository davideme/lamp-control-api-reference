<?php
// filepath: src/php/tests/E2E/LampApiE2ETest.php

use PHPUnit\Framework\TestCase;

class LampApiE2ETest extends TestCase
{
    private static $serverProcess;
    private static $serverPort;
    private static $baseUrl = 'http://localhost:8080'; // Adjust if your API runs elsewhere

    public static function setUpBeforeClass(): void
    {
        // Find a random available port
        $sock = stream_socket_server('tcp://127.0.0.1:0');
        $address = stream_socket_get_name($sock, false);
        fclose($sock);
        $parts = explode(':', $address);
        $port = end($parts);
        self::$serverPort = $port;
        self::$baseUrl = "http://127.0.0.1:$port";

        // Start PHP built-in server in background
        $docRoot = realpath(__DIR__ . '/../../../lamp-control-api/public');
        $cmd = sprintf(
            'php -S 127.0.0.1:%d -t %s > /dev/null 2>&1 & echo $!',
            $port,
            escapeshellarg($docRoot)
        );
        $output = [];
        exec($cmd, $output);
        self::$serverProcess = (int)($output[0] ?? 0);
        // Wait for server to be ready
        $maxTries = 10;
        $ready = false;
        while ($maxTries-- > 0) {
            if (@file_get_contents(self::$baseUrl . '/')) {
                $ready = true;
                break;
            }
            usleep(200000); // 200ms
        }
        if (!$ready) {
            self::tearDownAfterClass();
            throw new \RuntimeException('Failed to start server');
        }
    }

    public static function tearDownAfterClass(): void
    {
        if (self::$serverProcess) {
            exec('kill ' . self::$serverProcess);
        }
    }

    public function testCreateListGetUpdateDeleteLamp()
    {
        // 1. Create lamp
        $createResponse = $this->apiRequest('POST', '/lamps', ['status' => true]);
        $this->assertEquals(201, $createResponse['status']);
        $lamp = $createResponse['body'];
        $this->assertArrayHasKey('id', $lamp);
        $lampId = $lamp['id'];
        $this->assertTrue($lamp['status']);

        // 2. List lamps
        $listResponse = $this->apiRequest('GET', '/lamps');
        $this->assertEquals(200, $listResponse['status']);
        $lamps = $listResponse['body'];
        $this->assertNotEmpty($lamps);
        $this->assertEquals($lampId, $lamps[0]['id']);

        // 3. Get lamp
        $getResponse = $this->apiRequest('GET', "/lamps/{$lampId}");
        $this->assertEquals(200, $getResponse['status']);
        $this->assertEquals($lampId, $getResponse['body']['id']);

        // 4. Update lamp
        $updateResponse = $this->apiRequest('PUT', "/lamps/{$lampId}", ['status' => false]);
        $this->assertEquals(200, $updateResponse['status']);
        $this->assertFalse($updateResponse['body']['status']);

        // 5. Delete lamp
        $deleteResponse = $this->apiRequest('DELETE', "/lamps/{$lampId}");
        $this->assertEquals(204, $deleteResponse['status']);

        // 6. Get deleted lamp (should 404)
        $getDeletedResponse = $this->apiRequest('GET', "/lamps/{$lampId}");
        $this->assertEquals(404, $getDeletedResponse['status']);
    }

    private function apiRequest($method, $path, $body = null)
    {
        $url = self::$baseUrl . $path;
        $opts = [
            'http' => [
                'method' => $method,
                'header' => "Content-Type: application/json\r\nAccept: application/json\r\n",
                'ignore_errors' => true,
            ]
        ];
        if ($body !== null) {
            $opts['http']['content'] = json_encode($body);
        }
        $context = stream_context_create($opts);
        $response = file_get_contents($url, false, $context);
        $statusLine = $http_response_header[0] ?? '';
        preg_match('{HTTP/\S+ (\d+)}', $statusLine, $match);
        $status = isset($match[1]) ? (int)$match[1] : 0;
        $decoded = json_decode($response, true);
        return [
            'status' => $status,
            'body' => $decoded,
        ];
    }
}
