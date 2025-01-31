rootProject.name = "aoc-y24"

include("shared")

// API Functions for interacting with adventofcode.com
include("api-update-session-cookie")
include("api-get-session-cookie")
include("api-get-input")

// Solution functions that actually solve the provided input
include("solution-historian-hysteria")
include("solution-red-nosed-reports")
include("solution-mull-it-over")
include("solution-ceres-search")
include("solution-print-queue")
include("solution-guard-gallivant")
include("solution-bridge-repair")
include("solution-resonant-collinearity")
include("solution-disk-fragmenter")
include("solution-hoof-it")