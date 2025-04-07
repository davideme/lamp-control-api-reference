export const openApiDocument = {
  openapi: '3.0.0',
  info: {
    title: 'Lamp Control API',
    version: '1.0.0',
    description: 'REST API for controlling smart lamps',
  },
  servers: [
    {
      url: '/api',
      description: 'API base URL',
    },
  ],
  components: {
    schemas: {
      Lamp: {
        type: 'object',
        properties: {
          id: {
            type: 'string',
            format: 'uuid',
            description: 'Unique identifier for the lamp',
          },
          name: {
            type: 'string',
            minLength: 1,
            maxLength: 100,
            description: 'Name of the lamp',
          },
          isOn: {
            type: 'boolean',
            description: 'Current state of the lamp',
          },
          brightness: {
            type: 'number',
            minimum: 0,
            maximum: 100,
            description: 'Brightness level (0-100)',
          },
          color: {
            type: 'string',
            pattern: '^#[0-9A-Fa-f]{6}$',
            description: 'Color in hex format (#RRGGBB)',
          },
          createdAt: {
            type: 'string',
            format: 'date-time',
            description: 'Creation timestamp',
          },
          updatedAt: {
            type: 'string',
            format: 'date-time',
            description: 'Last update timestamp',
          },
        },
        required: ['id', 'name', 'isOn', 'brightness', 'color', 'createdAt', 'updatedAt'],
      },
      CreateLampRequest: {
        type: 'object',
        properties: {
          name: {
            type: 'string',
            minLength: 1,
            maxLength: 100,
          },
          brightness: {
            type: 'number',
            minimum: 0,
            maximum: 100,
          },
          color: {
            type: 'string',
            pattern: '^#[0-9A-Fa-f]{6}$',
          },
        },
        required: ['name'],
      },
      UpdateLampRequest: {
        type: 'object',
        properties: {
          name: {
            type: 'string',
            minLength: 1,
            maxLength: 100,
          },
          brightness: {
            type: 'number',
            minimum: 0,
            maximum: 100,
          },
          color: {
            type: 'string',
            pattern: '^#[0-9A-Fa-f]{6}$',
          },
        },
      },
      Error: {
        type: 'object',
        properties: {
          error: {
            type: 'string',
          },
          message: {
            type: 'string',
          },
        },
        required: ['error', 'message'],
      },
    },
  },
  paths: {
    '/lamps': {
      get: {
        summary: 'Get all lamps',
        operationId: 'getAllLamps',
        responses: {
          '200': {
            description: 'List of lamps',
            content: {
              'application/json': {
                schema: {
                  type: 'array',
                  items: {
                    $ref: '#/components/schemas/Lamp',
                  },
                },
              },
            },
          },
          '500': {
            description: 'Internal server error',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
        },
      },
      post: {
        summary: 'Create a new lamp',
        operationId: 'createLamp',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                $ref: '#/components/schemas/CreateLampRequest',
              },
            },
          },
        },
        responses: {
          '201': {
            description: 'Lamp created successfully',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Lamp',
                },
              },
            },
          },
          '400': {
            description: 'Validation error',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
          '500': {
            description: 'Internal server error',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
        },
      },
    },
    '/lamps/{id}': {
      parameters: [
        {
          name: 'id',
          in: 'path',
          required: true,
          schema: {
            type: 'string',
            format: 'uuid',
          },
          description: 'Lamp ID',
        },
      ],
      get: {
        summary: 'Get a lamp by ID',
        operationId: 'getLampById',
        responses: {
          '200': {
            description: 'Lamp found',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Lamp',
                },
              },
            },
          },
          '404': {
            description: 'Lamp not found',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
          '500': {
            description: 'Internal server error',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
        },
      },
      patch: {
        summary: 'Update a lamp',
        operationId: 'updateLamp',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                $ref: '#/components/schemas/UpdateLampRequest',
              },
            },
          },
        },
        responses: {
          '200': {
            description: 'Lamp updated successfully',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Lamp',
                },
              },
            },
          },
          '400': {
            description: 'Validation error',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
          '404': {
            description: 'Lamp not found',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
          '500': {
            description: 'Internal server error',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
        },
      },
      delete: {
        summary: 'Delete a lamp',
        operationId: 'deleteLamp',
        responses: {
          '204': {
            description: 'Lamp deleted successfully',
          },
          '404': {
            description: 'Lamp not found',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
          '500': {
            description: 'Internal server error',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
        },
      },
    },
    '/lamps/{id}/toggle': {
      parameters: [
        {
          name: 'id',
          in: 'path',
          required: true,
          schema: {
            type: 'string',
            format: 'uuid',
          },
          description: 'Lamp ID',
        },
      ],
      post: {
        summary: 'Toggle a lamp on/off',
        operationId: 'toggleLamp',
        responses: {
          '200': {
            description: 'Lamp toggled successfully',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Lamp',
                },
              },
            },
          },
          '404': {
            description: 'Lamp not found',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
          '500': {
            description: 'Internal server error',
            content: {
              'application/json': {
                schema: {
                  $ref: '#/components/schemas/Error',
                },
              },
            },
          },
        },
      },
    },
  },
}; 