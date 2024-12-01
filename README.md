# Advent of Code 2024

A needlessly verbose project for Advent of Code.

* The solutions are written in Java and run in serverless [Micronaut](https://micronaut.io/) applications deployed on
  [AWS Lambda](https://aws.amazon.com/lambda/).
* They can be reached via [Amazon API Gateway](https://aws.amazon.com/api-gateway/), secured
  via [Amazon Cognito](https://aws.amazon.com/de/cognito/) Oauth
  2.0.
* Puzzle inputs are retrieved automatically and cached in Amazon [DynamoDB](https://aws.amazon.com/dynamodb/).
* The build process is largely automated using [Gradle](https://gradle.org/) and the deployment process relies on
  [Terraform](https://www.terraform.io/).

### Solution Tests

Since puzzle inputs [are not be shared](https://adventofcode.com/2024/about), the solution tests are set up with files
not pushed to git. **If you are _insane_ enough** to clone this repo and want to build it locally, you will have to
ignore the test failures or add your own local inputs and
solutions. Simply drop the `input` file into `<module>/src/test/resources`. Oh, and don't forget the `solution` JSON!

## Building

Simply run `./gradlew build test shadowJar`
The lambda-ready jars should appear in `<module>/build/libs/<module>-lambda.jar`

## Solution Modules

Each day has its own Micronaut function. Please consult this convenient table.

| Day | Function                                                                                                                                                 |
|-----|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| 01  | [solution-historian-hysteria](solution-historian-hysteria/src/main/java/com/mostlynobody/aoc/y24/solution/historianhysteria/FunctionRequestHandler.java) |
| 02  | TBD...                                                                                                                                                   |

## To-Do List

- [x] Create Lambda Functions for caching the session cookie and inputs (Kinda done)
- [x] Create a Framework for actually running and testing the solutions
- [ ] Create Terraform files for automatic deployment
- [ ] Add RenovateBot for automatic dependency updates
- [ ] Add Oauth2 using Cognito
- [ ] Wrap everything in a step function
- [ ] Add absolutely necessary diagrams