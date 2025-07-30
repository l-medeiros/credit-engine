#!/bin/bash

BASE_URL="http://localhost:8080"
BATCH_SIZE="${1:-10000}"

ENDPOINT="$BASE_URL/simulations/batch"

echo "=== LOAD TEST ==="
echo "URL: $ENDPOINT"
echo "Batch size: $BATCH_SIZE applications"
echo

# Create JSON payload
echo "Creating payload with $BATCH_SIZE applications..."
cat > payload.json << EOF
{
  "loanApplications": [
EOF

# Generate loan applications
for i in $(seq 1 $BATCH_SIZE); do
  if [ $i -eq $BATCH_SIZE ]; then
    echo '    {"amount": 10000.0, "installments": 12, "birthdate": "01/01/1993"}' >> payload.json
  else
    echo '    {"amount": 10000.0, "installments": 12, "birthdate": "01/01/1993"},' >> payload.json
  fi
done

echo '  ]' >> payload.json
echo '}' >> payload.json

echo "✓ Payload created"
echo

# Execute the request
echo "Sending request..."
start_time=$(date +%s.%N)

response_file=$(mktemp)
status_code=$(curl -s -w "%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d @payload.json \
  -o "$response_file" \
  "$ENDPOINT")

end_time=$(date +%s.%N)

# Read response body
response_body=$(cat "$response_file")
rm "$response_file"

# Calculate total time
total_time=$(echo "$end_time - $start_time" | bc -l)

echo
echo "=== RESULTS ==="
echo "HTTP Status: $status_code"
echo "Total time: ${total_time}s"
echo "Applications processed: $BATCH_SIZE"
echo "Applications per second: $(echo "scale=2; $BATCH_SIZE / $total_time" | bc -l)"
echo "Response body: $response_body"
echo

if [ "$status_code" = "202" ] || [ "$status_code" = "200" ] || [ "$status_code" = "201" ]; then
    echo "✓ Request successful (Status: $status_code)"
else
    echo "✗ Request failed (Status: $status_code)"
    echo "Response: $response_body"
fi

# Clean up temporary file
rm -f payload.json

echo
echo "Test completed!"