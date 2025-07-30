#!/bin/bash

BASE_URL="http://localhost:8080"
BATCH_SIZE="${1:-10000}"
POLL_INTERVAL=2

ENDPOINT="$BASE_URL/simulations/batch"

echo "=== LOAD TEST ==="
echo "URL: $ENDPOINT"
echo "Batch size: $BATCH_SIZE applications"
echo "Poll interval: ${POLL_INTERVAL}s"
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

# Execute the initial request
echo "Sending batch request..."
request_start_time=$(date +%s.%N)

response_file=$(mktemp)
status_code=$(curl -s -w "%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d @payload.json \
  -o "$response_file" \
  "$ENDPOINT")

request_end_time=$(date +%s.%N)

# Read response body
response_body=$(cat "$response_file")
rm "$response_file"

# Calculate request time
request_time=$(echo "$request_end_time - $request_start_time" | bc -l)

echo "✓ Request sent in ${request_time}s"
echo "HTTP Status: $status_code"
echo

if [ "$status_code" != "202" ] && [ "$status_code" != "200" ] && [ "$status_code" != "201" ]; then
    echo "✗ Request failed (Status: $status_code)"
    echo "Response: $response_body"
    rm -f payload.json
    exit 1
fi

# Extract batchId from response
batch_id=$(echo "$response_body" | grep -o '"batchId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$batch_id" ]; then
    echo "✗ Could not extract batchId from response"
    echo "Response: $response_body"
    rm -f payload.json
    exit 1
fi

echo "Batch ID: $batch_id"
echo "Monitoring batch status..."
echo

# Start monitoring batch status
processing_start_time=$(date +%s.%N)
status_checks=0

while true; do
    status_checks=$((status_checks + 1))
    
    # Get batch status
    status_response_file=$(mktemp)
    status_code=$(curl -s -w "%{http_code}" \
        -o "$status_response_file" \
        "$BASE_URL/simulations/batch/$batch_id")
    
    status_response=$(cat "$status_response_file")
    rm "$status_response_file"
    
    if [ "$status_code" != "200" ]; then
        echo "✗ Failed to get batch status (Status: $status_code)"
        break
    fi
    
    # Extract all data from response
    batch_status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    processed_count=$(echo "$status_response" | grep -o '"processedApplications":[0-9]*' | cut -d':' -f2)
    completed_count=$(echo "$status_response" | grep -o '"completedSimulations":[0-9]*' | cut -d':' -f2)
    failed_count=$(echo "$status_response" | grep -o '"failedSimulations":[0-9]*' | cut -d':' -f2)
    created_at=$(echo "$status_response" | grep -o '"createdAt":"[^"]*"' | cut -d'"' -f4)
    completed_at=$(echo "$status_response" | grep -o '"completedAt":"[^"]*"' | cut -d'"' -f4)
    
    current_time=$(date +%s.%N)
    elapsed_time=$(echo "$current_time - $processing_start_time" | bc -l)
    
    printf "\r[%02d:%02d] Status: %-12s | Processed: %s/%s | Approved: %s | Rejected: %s | Checks: %d" \
        $((${elapsed_time%.*} / 60)) \
        $((${elapsed_time%.*} % 60)) \
        "$batch_status" \
        "$processed_count" \
        "$BATCH_SIZE" \
        "${completed_count:-0}" \
        "${failed_count:-0}" \
        "$status_checks"
    
    if [ "$batch_status" = "COMPLETED" ] || [ "$batch_status" = "FAILED" ]; then
        echo
        break
    fi
    
    sleep "$POLL_INTERVAL"
done

processing_end_time=$(date +%s.%N)
total_processing_time=$(echo "$processing_end_time - $processing_start_time" | bc -l)
total_time=$(echo "$processing_end_time - $request_start_time" | bc -l)

# Evaluate job time to process
if [ -n "$created_at" ] && [ -n "$completed_at" ]; then
    created_epoch=$(date -j -f "%Y-%m-%dT%H:%M:%S" "${created_at%.*}" "+%s" 2>/dev/null || echo "")
    completed_epoch=$(date -j -f "%Y-%m-%dT%H:%M:%S" "${completed_at%.*}" "+%s" 2>/dev/null || echo "")
    
    if [ -n "$created_epoch" ] && [ -n "$completed_epoch" ]; then
        actual_processing_time=$(echo "$completed_epoch - $created_epoch" | bc -l)
    fi
fi

echo
echo "=== FINAL RESULTS ==="
echo "Batch Status: $batch_status"
echo "Request Time: ${request_time}s"
echo "Monitoring Time: ${total_processing_time}s"
if [ -n "$actual_processing_time" ]; then
    echo "Actual Processing Time: ${actual_processing_time}s"
fi
echo "Total Time: ${total_time}s"
echo "Applications: $BATCH_SIZE"
echo "Apps/sec (total): $(echo "scale=2; $BATCH_SIZE / $total_time" | bc -l)"
if [ -n "$actual_processing_time" ] && [ "$(echo "$actual_processing_time > 0" | bc -l)" = "1" ]; then
    echo "Apps/sec (actual): $(echo "scale=2; $BATCH_SIZE / $actual_processing_time" | bc -l)"
fi
echo "Status Checks: $status_checks"
echo "Batch ID: $batch_id"
echo

if [ "$batch_status" = "COMPLETED" ]; then
    echo "✓ Batch completed successfully!"
    
    completed_count=${completed_count:-0}
    failed_count=${failed_count:-0}
    
    echo "Approved: $completed_count"
    echo "Rejected: $failed_count"
    
    if [ "$BATCH_SIZE" -gt 0 ] && [ "$completed_count" -ge 0 ]; then
        approval_rate=$(echo "scale=2; $completed_count * 100 / $BATCH_SIZE" | bc -l 2>/dev/null)
        if [ $? -eq 0 ] && [ -n "$approval_rate" ]; then
            echo "Approval Rate: ${approval_rate}%"
        else
            echo "Approval Rate: 0%"
        fi
    else
        echo "Approval Rate: 0%"
    fi
    
    if [ -n "$created_at" ]; then
        echo "Created At: $created_at"
    fi
    if [ -n "$completed_at" ]; then
        echo "Completed At: $completed_at"
    fi
else
    echo "✗ Batch failed or was interrupted"
fi

# Clean up
rm -f payload.json

echo
echo "Load test completed!"