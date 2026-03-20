# Cloud Run Deployment

Each language implementation has a `service.yaml` (Knative format) that defines its Cloud Run service.

## Services

| Language   | Service name                  |
|------------|-------------------------------|
| Go         | `go-lamp-control-api`         |
| TypeScript | `typescript-lamp-control-api` |
| Python     | `python-lamp-control-api`     |
| Java       | `java-lamp-control-api`       |
| Kotlin     | `kotlin-lamp-control-api`     |
| C#         | `csharp-lamp-control-api`     |

## Prerequisites

Each service runs an OTel collector sidecar that reads its config from a GCP Secret Manager secret. This secret must exist before deploying.

### Create the OTel collector secret (one-time setup)

```bash
gcloud secrets create otel-collector-config
gcloud secrets versions add otel-collector-config \
  --data-file=otel-collector-config-gcp.yaml
```

## Deploying services

The `service.yaml` files contain no deployment-specific values. Pass your region via the `--region` flag:

```bash
gcloud run services replace src/<language>/service.yaml --region=<region>
```

Deploy all 6 at once:

```bash
for lang in go typescript python java kotlin csharp; do
  gcloud run services replace src/$lang/service.yaml --region=<region>
done
```

## Making services publicly accessible

Cloud Run services default to requiring authentication. Grant public access with:

```bash
gcloud run services add-iam-policy-binding <service-name> \
  --region=<region> \
  --member="allUsers" \
  --role="roles/run.invoker"
```

All 6 at once:

```bash
for service in go-lamp-control-api typescript-lamp-control-api python-lamp-control-api java-lamp-control-api kotlin-lamp-control-api csharp-lamp-control-api; do
  gcloud run services add-iam-policy-binding $service \
    --region=<region> \
    --member="allUsers" \
    --role="roles/run.invoker"
done
```

> `add-iam-policy-binding` is preferred over `set-iam-policy` because it merges the new binding into the existing policy rather than replacing it entirely.

## Cloud Build (CI/CD)

Each language has a `cloudbuild.yaml` that builds, pushes, and deploys the app container on every push. The YAML contains no deployment-specific values — all substitution variables (`_AR_HOSTNAME`, `_AR_PROJECT_ID`, `_AR_REPOSITORY`, `_DEPLOY_REGION`, `_SERVICE_NAME`, `_TRIGGER_ID`) are configured in the Cloud Build trigger.
