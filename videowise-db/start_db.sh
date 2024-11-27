docker build -t videowise-db .
docker run -d \
  --name videowise-db \
  -v $(pwd)/db_data:/var/lib/postgresql/data \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=videowise_db \
  -p 5432:5432 \
  videowise-db