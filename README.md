# eidas-proxy
Norwegian generic eIDAS-proxy build from eIDAS source.

See these documents in https://ec.europa.eu/digital-building-blocks/sites/display/DIGITAL/eIDAS-Node+version+2.7.1:
* eIDAS-Node National IdP and SP Integration Guide v2.7.pdf
* eIDAS-Node Installation Quick Start Guide v2.7.pdf
* eIDAS-Node Installation and Configuration Guide v2.7.1.pdf


## Configuration
See [docker/proxy/README.md](docker/proxy/README.md) for organization of folders for config.

| Environment    | Keystore type |
|----------------|---------------|
| docker (local) | RSA keystore  |
| systest        | RSA keystore  |
| test           | HSM (luna)    |
| prod           | HSM (luna)    |

### Run eidas-proxy as docker-compose on your machine for local testing

Add the following to your /etc/hosts file:
```
# eIDAS local dev
127.0.0.1 eidas-proxy
```

Start docker containers:
```
docker-compose up --build 
```

Test by checking if metadata-endepoint is responding: eidas-proxy:8082/ServiceMetadata



### Test eidas-proxy in test/production environment
Systest: https://proxy.eidasnode.dev/ServiceMetadata

Test: https://proxy.test.eidasnode.no/ServiceMetadata

Prod: https://proxy.eidasnode.no/ServiceMetadata
