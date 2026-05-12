# Runbook: Embedding Model Upgrade

## Overview
Procedure for upgrading the embedding model (e.g., text-embedding-3-small → text-embedding-3-large).

## Prerequisites
- Access to Kubernetes cluster
- Access to model registry or API key for new model
- New model dimension must be known

## Pre-upgrade Checklist
- [ ] Verify new model is available and API key has sufficient quota
- [ ] Note current model version: `kubectl get configmap -n aimemory embedding-config`
- [ ] Backup Weaviate data (optional but recommended)
- [ ] Notify users of potential brief unavailability

## Step-by-Step Procedure

### 1. Register New Model Version

```bash
# Update embedding-service configmap
kubectl set env deployment/embedding-service \
  EMBEDDING_MODEL_NAME=text-embedding-3-large \
  EMBEDDING_MODEL_VERSION=v2 \
  EMBEDDING_MODEL_DIMENSION=3072 -n aimemory
```

### 2. Trigger Reindex Pipeline

The reindex is triggered automatically when the active model changes via ModelVersionRegistry:

```bash
# Verify reindex event published
kubectl logs -l app=embedding-service -n aimemory | grep "reindex.triggered"
```

### 3. Monitor Reindex Progress

```bash
# Check reindex job status
kubectl get jobs -n aimemory | grep reindex
kubectl logs job/reindex-batch-job -n aimemory

# Monitor Weaviate vector count
weaviate-cli count vectors --class Memory_<tenant_id>
```

### 4. Verify Completion

```bash
# Check all memories have new embedding version
psql -h postgres -U aimemory -d aimemory -c \
  "SELECT embedding_model_version, COUNT(*) FROM memories GROUP BY embedding_model_version;"
```

### 5. Rollback (if needed)

```bash
# If issues occur, rollback to previous model
kubectl set env deployment/embedding-service \
  EMBEDDING_MODEL_NAME=text-embedding-3-small \
  EMBEDDING_MODEL_VERSION=v1 \
  EMBEDDING_MODEL_DIMENSION=1536 -n aimemory
```

## Post-upgrade Verification
- [ ] Run retrieval tests to verify quality
- [ ] Monitor latency metrics (should remain < 400ms P99)
- [ ] Check embedding-service logs for errors

## Time Estimate
- Pre-upgrade: 10 minutes
- Reindex (10M records): ~4 hours
- Post-upgrade verification: 30 minutes

## Troubleshooting

### Reindex Fails
1. Check embedding-service logs for OpenAI API errors
2. Verify API key has sufficient quota
3. Retry with smaller batch size

### Latency Increases
1. New model may have different latency profile
2. Adjust retrieval timeout if needed
3. Consider adding cache layer