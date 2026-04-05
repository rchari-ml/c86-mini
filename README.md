# c86-mini

Run Camunda 8.7.x with Elastic in two separate namespaces respectively

```bash
kubectl apply -f namespace-elastic.yaml
kubectl apply -f namespace-c86-mini.yaml

sh elk-makefile

helm install elasticsearch elastic/elasticsearch -n elastic -f elasticsearch.yaml

kubectl apply -f identity-secret.yaml --namespace c86-mini

# replace user id, email, token appropriately
kubectl create secret docker-registry registry-camunda-cloud  \
--namespace=c86-mini \
--docker-server=registry.camunda.cloud \
    --docker-username=user-id-here \
    --docker-password=secret-here \
    --docker-email=email-here

# replace user id, email, token appropriately
kubectl create secret docker-registry registry-docker-hub  \
    --namespace=c86-mini \
    --docker-server=hub.docker.com \
    --docker-username=user-id-here \
    --docker-password=secret-here \
    --docker-email=email-here

helm repo add camunda https://helm.camunda.io
helm repo update
helm install c86-mini camunda/camunda-platform \
  -f values-combined-ingress.yaml \
  -f values-connectors-env.yaml \
  --set zeebe.prometheusServiceMonitor.enabled=true \
  --version 12.4.0 --namespace c86-mini

# Cleanup - uninstall camunda
helm uninstall c86-mini camunda/camunda-platform     --namespace c86-mini
```

