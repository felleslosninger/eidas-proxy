export $(/vault/vault-env env | grep KEYSTORE_PASSWORD | xargs)
keytool -list -v -keystore lunastore.txt -storetype Luna -storepass $KEYSTORE_PASSWORD -providerpath "/var/usrlocal/luna/jsp/LunaProvider.jar" -providerclass com.safenetinc.luna.provider.LunaProvider -J-Djava.library.path=/opt/java/openjdk/lib/ -J-cp -J/var/usrlocal/luna/jsp/LunaProvider.jar

#
#keytool -list -v -keystore lunastore.txt -storetype Luna -storepass $KEYSTORE_PASSWORD

# funker:
#keytool -list -v -keystore lunastore.txt -storetype Luna -storepass $KEYSTORE_PASSWORD -providerpath "/var/usrlocal/luna/jsp/LunaProvider.jar" -providerclass com.safenetinc.luna.provider.LunaProvider

# Standard keytool kommando for pkcs11 når berre ein SunPKCS11 provider er konfigurert i security fila, då blir den provideren nytta.
# keytool -list -v -keystore NONE -storetype PKCS11 -storepass $KEYSTORE_PASSWORD