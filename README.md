# c86-mini

Run Camunda 8.7.x with Elastic in two separate namespaces respectively

```bash
kubectl apply -f namespace-elastic.yaml
kubectl apply -f namespace-c86-mini.yaml
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.1.0/standard-install.yaml

sh elk-makefile

helm install elasticsearch elastic/elasticsearch -n elastic -f elasticsearch.yaml


kubectl apply -f keycloak-secret.yaml --namespace c86-mini
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
  --namespace c86-mini  --version 14.0.1

helm upgrade c86-mini camunda/camunda-platform \
  -f values-combined-ingress.yaml \
  -f values-connectors-env.yaml \
  --set zeebe.prometheusServiceMonitor.enabled=true \
  --namespace c86-mini  --version 14.0.1


export TOKEN=$(curl https://c86-mini.makelabs.in/auth/realms/camunda-platform/protocol/openid-connect/token  \
   -H 'Content-Type: application/x-www-form-urlencoded' \
   -d 'grant_type=client_credentials' \
   -d 'client_id=orchestration'   -d 'client_secret=makelabs' | jq -r '.access_token' )

curl -v --request GET \
  --url 'https://c86-mini.makelabs.in/orchestration/v2/topology' \
  --header "Authorization: Bearer $TOKEN" -H 'Content-Type: application/x-www-form-urlencoded'


curl -v --request POST 'https://c86-mini.makelabs.in/orchestration/v2/deployments' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Accept: application/json' \
  -F resources=@new-lead-approval.dmn

curl -v --request POST \
  --url 'https://c86-mini.makelabs.in/orchestration/v2/deployments' \
  --header "Authorization: Bearer $TOKEN" \
  --header 'Content-Type: multipart/form-data' \
  --form 'resources=@lead-to-opportunity-with-dmn.bpmn'

curl -v --request POST \
  --url 'https://c86-mini.makelabs.in/orchestration/v2/deployments' \
  --header "Authorization: Bearer $TOKEN" \
  --header 'Content-Type: multipart/form-data' \
  --form 'resources=@lead-to-opportunity-with-webhook-Form.form'

# Cleanup - uninstall camunda & elastic
helm uninstall c86-mini camunda/camunda-platform     --namespace c86-mini
helm uninstall elasticsearch elastic/elasticsearch -n elastic  

# current user info
https://c86-mini.makelabs.in/orchestration/v2/authentication/me

```

# Work in progress


kubectl port-forward svc/c86-mini-zeebe-gateway 26500:26500 -n c86-mini
kubectl port-forward svc/c86-mini-zeebe-gateway 8080:8080 -n c86-mini


Identity:
kubectl port-forward svc/c86-mini-identity 8084:80 -n c86-mini
Optimize:
kubectl port-forward svc/c86-mini-optimize 8083:80 -n c86-mini

Connectors:
kubectl port-forward svc/c86-mini-connectors 8086:8080 -n c86-mini
Console:
kubectl port-forward svc/c86-mini-console 8087:80 -n c86-mini

KC Auth:
kubectl port-forward svc/c86-mini-keycloak 18080:80 -n c86-mini

Operate / Tasklist:
kubectl port-forward svc/c86-mini-zeebe 8081:8080 -n c86-mini 

Zeebe gRPC: (Testing with toolit service in progress)
kubectl port-forward svc/c86-mini-zeebe 26500:26500 -n c86-mini


Convert jks trust store to a pem file
keytool -list -rfc   -keystore externaldb.jks   -storepass changeit > truststore.pem
openssl crl2pkcs7 -nocrl -certfile truststore.pem | openssl pkcs7 -print_certs -noout

Using the trust store pem file, invoke api to fetch new bearer token
curl https://c86-mini.makelabs.in/auth/realms/camunda-platform/protocol/openid-connect/token   --cacert truststore.pem   -H 'Content-Type: application/x-www-form-urlencoded'   -d 'grant_type=client_credentials'   -d 'client_id=orchestration'   -d 'client_secret=secret-value'


Get full cert chain
openssl s_client -connect c86-mini.makelabs.in:443 -servername c86-mini.makelabs.in -showcerts

openssl s_client -connect c86-mini.makelabs.in:443 -servername c86-mini.makelabs.in -showcerts \
  </dev/null 2>/dev/null | sed -n '/BEGIN CERTIFICATE/,/END CERTIFICATE/p' > google-cert-chain.pem
