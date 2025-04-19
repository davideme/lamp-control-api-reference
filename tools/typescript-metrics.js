const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const sloc = require('sloc');
const { glob } = require('glob');

// Configuration
const SRC_DIR = '../src/typescript';  // Path relative to tools directory
const TEST_PATTERN = '../src/typescript/**/*.test.ts';
const APP_PATTERN = '../src/typescript/src/**/*.ts';
const OUTPUT_DIR = 'metrics_reports/typescript';

// Common ignore patterns
const COMMON_IGNORES = [
    '**/node_modules/**',
    '**/dist/**',
    '**/.git/**'
];

// Create output directory
if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

// Function to get detailed line counts using sloc
async function getDetailedLOC(pattern, excludeTests = false) {
    try {
        // Get all matching files
        const globOptions = {
            ignore: [
                ...COMMON_IGNORES,
                ...(excludeTests ? ['**/*.test.ts', '**/*.spec.ts'] : [])
            ],
            absolute: true
        };
        
        const files = await glob(pattern, globOptions);

        const fileMetrics = files.map(filePath => {
            const content = fs.readFileSync(filePath, 'utf8');
            const stats = sloc(content, 'js');  // Use 'js' for TypeScript files
            return {
                path: filePath,
                name: path.basename(filePath),
                stats: {
                    total: stats.total,           // Total lines
                    source: stats.source,         // Source code lines
                    comment: stats.comment,       // Comment lines
                    single: stats.single,         // Single-line comments
                    block: stats.block,           // Block comments
                    mixed: stats.mixed,           // Mixed lines (code + comment)
                    empty: stats.empty,           // Empty lines
                    todo: stats.todo,             // Todo comments
                    blockEmpty: stats.blockEmpty  // Empty lines in blocks
                }
            };
        }).sort((a, b) => b.stats.source - a.stats.source);

        // Calculate totals
        const totals = fileMetrics.reduce((acc, file) => {
            Object.keys(file.stats).forEach(key => {
                acc[key] = (acc[key] || 0) + file.stats[key];
            });
            return acc;
        }, {});

        // Calculate averages
        const fileCount = fileMetrics.length;
        const averages = {};
        Object.keys(totals).forEach(key => {
            averages[key] = Math.round(totals[key] / fileCount);
        });

        return {
            files: fileMetrics,
            totals,
            averages,
            fileCount
        };
    } catch (error) {
        console.error(`Error counting LOC for ${pattern}:`, error);
        return {
            files: [],
            totals: {},
            averages: {},
            fileCount: 0
        };
    }
}

// Function to get detailed Halstead metrics
async function getDetailedHalsteadMetrics(pattern, excludeTests = false) {
    try {
        const globOptions = {
            ignore: [
                ...COMMON_IGNORES,
                ...(excludeTests ? ['**/*.test.ts', '**/*.spec.ts'] : [])
            ],
            absolute: true
        };
        
        const files = await glob(pattern, globOptions);
        
        if (files.length === 0) {
            return null;
        }

        // Convert file paths to space-separated string, properly escaped
        const filesArg = files.map(f => `"${f}"`).join(' ');
        const result = execSync(`npx ts-complexity ${filesArg} --json`, { 
            encoding: 'utf8',
            maxBuffer: 1024 * 1024 * 10 // 10MB buffer
        });
        
        if (!result.trim()) {
            console.error('ts-complexity returned empty result');
            return null;
        }

        const metrics = JSON.parse(result);
        
        const fileMetrics = metrics.map(file => ({
            path: file.path,
            name: path.basename(file.path),
            halstead: file.halstead || null
        }))
        .filter(file => file.halstead)
        .sort((a, b) => b.halstead.difficulty - a.halstead.difficulty);

        if (fileMetrics.length === 0) {
            return null;
        }

        // Calculate aggregated metrics
        const aggregated = fileMetrics.reduce((acc, file) => {
            const h = file.halstead;
            acc.length = (acc.length || 0) + h.length;
            acc.vocabulary = (acc.vocabulary || 0) + h.vocabulary;
            acc.volume = (acc.volume || 0) + h.volume;
            acc.difficulty = (acc.difficulty || 0) + h.difficulty;
            acc.effort = (acc.effort || 0) + h.effort;
            acc.time = (acc.time || 0) + h.time;
            acc.bugs = (acc.bugs || 0) + h.bugs;
            acc.fileCount = (acc.fileCount || 0) + 1;
            return acc;
        }, {});

        // Calculate averages
        const averages = {
            difficulty: aggregated.difficulty / aggregated.fileCount,
            bugs: aggregated.bugs / aggregated.fileCount,
            effort: aggregated.effort / aggregated.fileCount,
            volume: aggregated.volume / aggregated.fileCount
        };

        return {
            total: aggregated,
            averages,
            files: fileMetrics
        };
    } catch (error) {
        console.error(`Error getting Halstead metrics for ${pattern}:`, error);
        if (error.stdout) console.error('stdout:', error.stdout);
        if (error.stderr) console.error('stderr:', error.stderr);
        return null;
    }
}

// Main function to run metrics
async function runMetrics() {
    console.log('Analyzing TypeScript metrics...\n');

    // Get detailed metrics
    const appLOC = await getDetailedLOC(APP_PATTERN, true);  // Exclude test files
    const testLOC = await getDetailedLOC(TEST_PATTERN);
    const appHalstead = await getDetailedHalsteadMetrics(APP_PATTERN, true);  // Exclude test files
    const testHalstead = await getDetailedHalsteadMetrics(TEST_PATTERN);

    // Generate report
    const report = {
        summary: {
            application: {
                files: appLOC.fileCount,
                lines: appLOC.totals
            },
            tests: {
                files: testLOC.fileCount,
                lines: testLOC.totals
            },
            ratios: {
                testToCode: testLOC.totals.source ? (testLOC.totals.source / appLOC.totals.source).toFixed(2) : 0,
                commentToCode: appLOC.totals.source ? (appLOC.totals.comment / appLOC.totals.source).toFixed(2) : 0
            }
        },
        application: {
            lineMetrics: {
                totals: appLOC.totals,
                averages: appLOC.averages,
                files: appLOC.files
            },
            halstead: appHalstead
        },
        tests: {
            lineMetrics: {
                totals: testLOC.totals,
                averages: testLOC.averages,
                files: testLOC.files
            },
            halstead: testHalstead
        }
    };

    // Save detailed report
    fs.writeFileSync(
        `${OUTPUT_DIR}/metrics-${new Date().toISOString().split('T')[0]}.json`,
        JSON.stringify(report, null, 2)
    );

    // Print detailed summary
    console.log('TYPESCRIPT CODE METRICS BREAKDOWN\n');

    console.log('SUMMARY');
    console.log('-------');
    console.log(`Total Files: ${report.summary.application.files + report.summary.tests.files}`);
    console.log(`Application Files: ${report.summary.application.files}`);
    console.log(`Test Files: ${report.summary.tests.files}\n`);

    console.log('APPLICATION CODE');
    console.log('---------------');
    if (report.summary.application.lines) {
        console.log('Lines of Code:');
        console.log(`  Total Lines: ${report.summary.application.lines.total}`);
        console.log(`  Source Code Lines: ${report.summary.application.lines.source}`);
        console.log(`  Comment Lines: ${report.summary.application.lines.comment}`);
        console.log(`  Empty Lines: ${report.summary.application.lines.empty}`);
        console.log(`  Mixed Lines (code + comment): ${report.summary.application.lines.mixed}`);
        console.log('\nTop 5 Largest Files (by source lines):');
        report.application.lineMetrics.files.slice(0, 5).forEach(file => {
            console.log(`- ${file.name}: ${file.stats.source} source lines (${file.stats.total} total)`);
        });
    }

    console.log('\nTEST CODE');
    console.log('---------');
    if (report.summary.tests.lines) {
        console.log('Lines of Code:');
        console.log(`  Total Lines: ${report.summary.tests.lines.total}`);
        console.log(`  Source Code Lines: ${report.summary.tests.lines.source}`);
        console.log(`  Comment Lines: ${report.summary.tests.lines.comment}`);
        console.log(`  Empty Lines: ${report.summary.tests.lines.empty}`);
        console.log(`  Mixed Lines (code + comment): ${report.summary.tests.lines.mixed}`);
        console.log('\nTop 5 Largest Test Files (by source lines):');
        report.tests.lineMetrics.files.slice(0, 5).forEach(file => {
            console.log(`- ${file.name}: ${file.stats.source} source lines (${file.stats.total} total)`);
        });
    }

    console.log('\nRATIOS');
    console.log('------');
    console.log(`Test to Code Ratio: ${report.summary.ratios.testToCode}`);
    console.log(`Comment to Code Ratio: ${report.summary.ratios.commentToCode}`);

    console.log('\nHALSTEAD METRICS COMPARISON');
    console.log('-------------------------');
    if (report.application.halstead?.averages && report.tests.halstead?.averages) {
        console.log('Average Metrics (Application vs Tests):');
        console.log(`Difficulty: ${report.application.halstead.averages.difficulty.toFixed(2)} vs ${report.tests.halstead.averages.difficulty.toFixed(2)}`);
        console.log(`Volume: ${report.application.halstead.averages.volume.toFixed(2)} vs ${report.tests.halstead.averages.volume.toFixed(2)}`);
        console.log(`Effort: ${report.application.halstead.averages.effort.toFixed(2)} vs ${report.tests.halstead.averages.effort.toFixed(2)}`);
        console.log(`Bugs: ${report.application.halstead.averages.bugs.toFixed(3)} vs ${report.tests.halstead.averages.bugs.toFixed(3)}`);
    }

    console.log('\nDetailed report saved to:', `${OUTPUT_DIR}/metrics-${new Date().toISOString().split('T')[0]}.json`);
}

// Run the metrics
runMetrics().catch(console.error); 