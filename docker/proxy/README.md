# Norwegian eIDAS-proxy Configuration

## Configuration
| Directory               | Description                                           |
|-------------------------|-------------------------------------------------------|
| `docker/proxy/config`   | eIDAS-proxy common configuration files.               |
| `docker/proxy/profiles` | eIDAS-proxy environment spesific configuration files. |
| `docker/luna`           | HMS configuration for test and production.            |
| `docker/bouncycastle`   | Bouncycastle java configuration files.                |


### Keystores
`docker/proxy/profiles/<ENVIRONMENT>/keystore/otherCountriesEidasKeyStore.p12` contains other countries certificates to trust.

## Integrate a new country
Exchange metadata signing certificates and add theirs to `docker/proxy/profiles/<ENVIRONMENT>/keystore/otherCountriesEidasKeyStore.p12`.

If encryption of communication add country ISO-code here: `docker/proxy/config/encryptionConf.xml`.


