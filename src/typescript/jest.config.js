/** @type {import('ts-jest').JestConfigWithTsJest} */
export default {
    preset: 'ts-jest',
    testEnvironment: 'node',
    roots: ["<rootDir>/src/", "<rootDir>/tests/"],
    extensionsToTreatAsEsm: ['.ts'],
    moduleNameMapper: {
        '^(\\.{1,2}/.*)\\.js$': '$1',
    },
    transform: {
        '^.+\\.tsx?$': [
            'ts-jest',
            {
                useESM: true,
                tsconfig: {
                    "forceConsistentCasingInFileNames": true,
                    "resolveJsonModule": true,
                    "module": "ESNext",
                    "moduleResolution": "bundler"
                }
            },
        ],
    },
    testMatch: ['**/*.test.ts', '**/tests/**/*.ts'],
    testTimeout: 60000, // Testcontainers need time to start
    collectCoverageFrom: [
        'src/**/*.ts',
        '!src/types/**/*.ts',
    ],
    coverageThreshold: {
        global: {
            branches: 80,
            functions: 80,
            lines: 80,
            statements: 80,
        },
    },
    coverageReporters: [
        "lcov",
        "text",
        "json",
        "json-summary"
      ],
};