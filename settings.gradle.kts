rootProject.name = "aoc-y24"

include("shared")

// API Functions for interacting with adventofcode.com
include("api-update-session-cookie")
include("api-get-session-cookie")

// Solution functions that actually solve the provided input
include("solution-historian-hysteria")