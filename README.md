# Pricing Service

A Typelevel-stack pricing microservice + DynamoDB Streams Lambda, built as a hands-on exercise for effect-oriented Scala on AWS.

## Tech Stack

- **Scala 3.6.4**, cats-effect 3, http4s, smithy4s, fs2
- **AWS**: DynamoDB, DynamoDB Streams, Lambda, Kinesis (via LocalStack)
- **Infra**: CDK (TypeScript), Docker Compose
- **Testing**: weaver, ScalaCheck, WireMock, testcontainers-scala

## Project Structure

```
api/       — Smithy IDL model + generated code (smithy4s)
core/      — Pure domain: types, validation, pricing (zero IO imports)
service/   — http4s server, DynamoDB repos, config, tracing
lambda/    — DynamoDB Streams → Kinesis processor
it/        — Integration tests (LocalStack via testcontainers)
infra/     — CDK app (TypeScript)
```

## Quick Start

```bash
# Start LocalStack
make up

# Deploy infra (DynamoDB tables, Kinesis stream, Lambda)
make deploy

# Seed test data
make seed

# Run unit + property tests
make test

# Run integration tests (requires LocalStack)
make test-integration

# Tear down
make down
```

## Architecture

```
POST /orders/price ──► Pricing API (http4s) ──► DynamoDB (Orders) ──► Streams trigger ──► Lambda (processor) ──► Kinesis stream
```

## Build

```bash
sbt compile      # Compile all modules (triggers smithy4s codegen)
sbt core/test    # Run pure unit + property tests
sbt test         # Run all tests
```
