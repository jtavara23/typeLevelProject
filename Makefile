.PHONY: up down compile test deploy test-integration seed clean

up:
	docker compose up -d
	@echo "Waiting for LocalStack to be ready..."
	@until curl -sf http://localhost:4566/_localstack/health > /dev/null 2>&1; do sleep 1; done
	@echo "LocalStack is ready."

down:
	docker compose down -v
	rm -rf localstack-data

compile:
	sbt compile

test:
	sbt test

deploy: up
	cd infra && npx cdklocal deploy --all --require-approval never

seed:
	@echo "Seeding Customers table..."
	awslocal dynamodb put-item \
		--table-name Customers \
		--item '{"customerId":{"S":"cust-123"},"tier":{"S":"GOLD"},"name":{"S":"Alice"},"createdAt":{"S":"2025-01-01T00:00:00Z"}}'
	awslocal dynamodb put-item \
		--table-name Customers \
		--item '{"customerId":{"S":"cust-456"},"tier":{"S":"BASIC"},"name":{"S":"Bob"},"createdAt":{"S":"2025-03-15T00:00:00Z"}}'
	@echo "Seeding Coupons table..."
	awslocal dynamodb put-item \
		--table-name Coupons \
		--item '{"couponCode":{"S":"SUMMER10"},"discountPercent":{"N":"10"},"minOrderAmount":{"N":"50"},"usageLimit":{"N":"100"},"usageCount":{"N":"42"},"expiresAt":{"S":"2027-09-01T00:00:00Z"},"stackableWithTier":{"SS":["GOLD","SILVER"]}}'
	awslocal dynamodb put-item \
		--table-name Coupons \
		--item '{"couponCode":{"S":"EXPIRED5"},"discountPercent":{"N":"5"},"minOrderAmount":{"N":"20"},"usageLimit":{"N":"50"},"usageCount":{"N":"50"},"expiresAt":{"S":"2025-01-01T00:00:00Z"},"stackableWithTier":{"SS":["GOLD","SILVER","BASIC"]}}'
	@echo "Seed data loaded."

test-integration: up deploy seed
	sbt it/test

clean:
	sbt clean
	rm -rf localstack-data
