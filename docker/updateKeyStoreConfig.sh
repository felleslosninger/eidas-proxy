#!/bin/bash

# NB: Passwords must be configured in idporten-cd in container.env
# Add passwords to config file for signing

# For keystore usage in local-docker and systest
SIGNMODULE_SERVICE_FILE=/etc/config/eidas-proxy/SignModule_Service.xml
if [ -f "$SIGNMODULE_SERVICE_FILE" ]; then
    echo "Update keystore-config in $SIGNMODULE_SERVICE_FILE" && printenv | grep PASSWORD
    sed -i "s/TRUSTSTORE_PASSWORD/$TRUSTSTORE_PASSWORD/g" $SIGNMODULE_SERVICE_FILE
    sed -i "s/KEYSTORE_PASSWORD/$KEYSTORE_PASSWORD/g" $SIGNMODULE_SERVICE_FILE
    sed -i "s/KEYSTORE_KEY_PASSWORD/$KEYSTORE_KEY_PASSWORD/g" $SIGNMODULE_SERVICE_FILE
    echo "Remove hsm security provider from java"
    sed 's/\SunPKCS11.*/SunPKCS11/g' /opt/java/openjdk/conf/security/java_bc.security > /opt/java/openjdk/conf/security/java_bc.security
fi

# HSM
# KEYSTORE_PASSWORD==HSM Password/pin
# HSM_ALIAS==alias/label in HSM
SIGNMODULE_SERVICE_FILE_HSM=/etc/config/eidas-proxy/SignModule_Service_HSM_P12.xml
if [ -f "$SIGNMODULE_SERVICE_FILE_HSM" ]; then
    echo "Update keystore-config in $SIGNMODULE_SERVICE_FILE_HSM" && printenv | grep HSM_ALIAS
    sed -i "s/TRUSTSTORE_PASSWORD/$TRUSTSTORE_PASSWORD/g" $SIGNMODULE_SERVICE_FILE_HSM
    sed -i "s/KEYSTORE_PASSWORD/$KEYSTORE_PASSWORD/g" $SIGNMODULE_SERVICE_FILE_HSM
    sed -i "s/HSM_ALIAS/$HSM_ALIAS/g" $SIGNMODULE_SERVICE_FILE_HSM
fi
