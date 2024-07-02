export $(/vault/vault-env env | grep KEYSTORE_PASSWORD | xargs)
keytool -list -v -keystore lunastore.txt -storetype Luna -storepass $KEYSTORE_PASSWORD -providerpath "/var/usrlocal/luna/jsp/LunaProvider.jar" -providerclass com.safenetinc.luna.provider.LunaProvider -J-Djava.library.path=/opt/java/openjdk/lib/ -J-cp -J/var/usrlocal/luna/jsp/LunaProvider.jar

#
#keytool -list -v -keystore lunastore.txt -storetype Luna -storepass $KEYSTORE_PASSWORD