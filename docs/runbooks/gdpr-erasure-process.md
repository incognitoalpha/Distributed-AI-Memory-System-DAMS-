# Runbook: GDPR Erasure Process

## Overview
Procedure for processing GDPR erasure requests within the 72-hour SLA.

## Prerequisites
- Access to Kubernetes cluster
- Access to compliance-service
- Database access for verification

## Erasure Request Flow

```
User Request → Compliance API → Memory Soft-Delete → Vector Deletion → Search Deletion → Audit Log → Complete
```

## Step-by-Step Procedure

### 1. Receive Erasure Request

The erasure request is received via API:
```
POST /api/v1/gdpr/erasure
{
  "userId": "uuid",
  "reason": "user_requested"
}
```

The system generates a request ID and queues the operation.

### 2. Verify Authorization

```bash
# Check request is authorized
kubectl logs -l app=compliance-service -n aimemory | grep "ERASURE_INITIATED"
```

### 3. Soft-Delete Memories (Immediate)

All user memories are soft-deleted in PostgreSQL:

```sql
-- Verified by audit log
SELECT * FROM audit_log WHERE event_type = 'ERASURE_INITIATED';
```

All memories now have:
- `soft_deleted = true`
- `soft_deleted_at = NOW()`
- `hard_delete_eligible_at = NOW() + 30 days`

### 4. Queue Vector Deletion (Async - Within 72h)

```bash
# Verify vector deletion event published
kubectl logs -l app=compliance-service -n aimemory | grep "VECTOR_DELETION_REQUESTED"
```

Weaviate vectors are deleted asynchronously:
```bash
# Check Weaviate for remaining user vectors
weaviate-cli find --class Memory_<tenant_id> --where '{"userId": "user-uuid"}'
```

### 5. Queue Search Deletion (Async - Within 72h)

```bash
# Verify OpenSearch deletion event published
kubectl logs -l app=compliance-service -n aimemory | grep "SEARCH_DELETION_REQUESTED"
```

OpenSearch documents deleted:
```bash
# Verify no documents remain
curl -X GET "http://opensearch:9200/memories/_search?q=userId:user-uuid"
```

### 6. Verify Completion

```bash
# Check retrieval returns no results
curl -X POST "http://retrieval-service:8083/api/v1/retrieval" \
  -H "X-Tenant-Id: tenant-uuid" \
  -d '{"query": "test"}'
# Should return empty results for erased user
```

### 7. Data Export (If Requested)

```bash
# User can request their data before erasure
GET /api/v1/gdpr/export/{userId}

# Returns all memories, versions, conflicts as JSON
```

## Verification Commands

### PostgreSQL
```sql
-- Verify soft-deleted
SELECT memory_id, soft_deleted, soft_deleted_at
FROM memories
WHERE user_id = 'user-uuid' AND tenant_id = 'tenant-uuid';
```

### Weaviate
```bash
weaviate-cli count vectors --class Memory_<tenant_id> --where '{"userId": "user-uuid"}'
# Should return 0
```

### OpenSearch
```bash
curl -X GET "http://opensearch:9200/memories/_search?q=userId:user-uuid"
# Should return 0 hits
```

## SLA Tracking

| Step | Deadline | Status Check |
|------|-----------|--------------|
| Memory soft-delete | Immediate | ✅ |
| Vector deletion | 72 hours | Monitor |
| Search deletion | 72 hours | Monitor |
| Audit log | Immediate | ✅ |

## Rollback

**Cannot rollback** - GDPR erasure is irreversible. Once completed:
- Memories cannot be recovered
- All vectors are permanently deleted
- All search index entries removed

## Contact
- Privacy Officer: privacy@company.com
- On-call: oncall@company.com