<?php

/**
 * Router script for PHP built-in web server
 * This ensures all API requests are properly routed to index.php
 * regardless of file extensions in the URL path
 */

$requestUri = $_SERVER['REQUEST_URI'];
$requestMethod = $_SERVER['REQUEST_METHOD'];

// Check if this is an API request (starts with /v1/)
if (strpos($requestUri, '/v1/') === 0) {
    // Route all API requests to index.php
    require __DIR__ . '/public/index.php';
    return true;
}

// Check if this is a health check
if ($requestUri === '/health') {
    require __DIR__ . '/public/index.php';
    return true;
}

// For non-API requests, let the built-in server handle them normally
return false;
