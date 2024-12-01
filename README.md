# Advent of Code 2024

A needlessly verbose project for Advent of Code.

The solutions are written in Java and run in serverless Micronaut applications deployed on AWS Lambda.

They can be reached via Amazon API Gateway, secured via Amazon Cognito Oauth 2.0.

Puzzle inputs are retrieved automatically and cached in Amazon DynamoDB.

The build process is largely automated using Gradle and the deployment process relies on Terraform.
