<?php

/**
 * Router script for PHP built-in web server
 * This ensures all API requests are properly routed to index.php
 * regardless of file extensions in the URL path or HTTP method
 */

// Guard against CLI invocation where certain server variables may be unset
$requestUri = $_SERVER['REQUEST_URI'] ?? '';
$requestMethod = $_SERVER['REQUEST_METHOD'] ?? 'GET';

// Parse the URI to remove query parameters (avoid deprecation by ensuring string input)
if ($requestUri !== '') {
    $parsedUri = parse_url($requestUri);
    $path = $parsedUri['path'] ?? $requestUri;
} else {
    $path = '/';
}

// Check if this is an API request (starts with /v1/)
if (strpos($path, '/v1/') === 0) {
    // Route all API requests to index.php regardless of HTTP method
    $_SERVER['REQUEST_URI'] = $requestUri; // Preserve original URI
    require __DIR__ . '/public/index.php';
    return true;
}

// Check if this is a health check
if ($path === '/health') {
    require __DIR__ . '/public/index.php';
    return true;
}

// For non-API requests, let the built-in server handle them normally
return false;
